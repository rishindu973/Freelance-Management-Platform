import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Clock } from "lucide-react";
import { ProjectService, ProjectResponse } from "@/data/projectService";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";

export function NearDeadlineProjects() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  const fetchProjects = async () => {
    try {
      setLoading(true);
      const data = await ProjectService.getAllProjects({ isCritical: true });
      const sortedData = data.sort(
        (a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime()
      );
      setProjects(sortedData);
    } catch (error) {
      console.error("Failed to fetch near deadline projects", error);
      toast({ title: "Error", description: "Could not load near deadline projects", variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleStatusChange = async (projectId: number, newStatus: string) => {
    try {
      await ProjectService.updateProject(projectId, { status: newStatus });
      toast({ title: "Success", description: "Project status updated" });
      fetchProjects();
    } catch (error) {
      toast({ title: "Error", description: "Failed to update status", variant: "destructive" });
    }
  };

  const getDaysRemaining = (deadline: string) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0); 
    const due = new Date(deadline);
    due.setHours(0, 0, 0, 0);
    return Math.floor((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  };

  const getUrgencyClasses = (days: number) => {
    if (days <= 2) return "bg-destructive/10 text-destructive border-destructive/20";
    if (days <= 5) return "bg-warning/10 text-warning border-warning/20";
    return "bg-success/10 text-success border-success/20";
  };

  const getUrgencyLabel = (days: number) => {
    if (days < 0) return `Overdue by ${Math.abs(days)} days!`;
    if (days === 0) return "Due today!";
    if (days === 1) return "Due tomorrow";
    return `Due in ${days} days`;
  };

  return (
    <div className="rounded-xl border bg-card p-5 shadow-sm">
      <div className="flex items-center gap-2">
        <Clock className="h-4 w-4 text-warning" />
        <h3 className="text-sm font-medium text-foreground">Upcoming Deadlines</h3>
      </div>
      <div className="mt-4 space-y-3">
        {loading ? (
          <p className="text-sm text-muted-foreground p-2">Loading...</p>
        ) : projects.length === 0 ? (
          <p className="text-sm text-muted-foreground p-2">No upcoming critical deadlines.</p>
        ) : (
          projects.map((project) => {
            const daysRemaining = getDaysRemaining(project.deadline);
            return (
              <div key={project.id} className="rounded-lg border bg-cream p-3 flex flex-col gap-2 transition-shadow hover:shadow-md">
                <div className="flex items-start justify-between">
                  <div className="flex-1 mr-2 overflow-hidden">
                    <Link to={`/projects/${project.id}`} className="text-sm font-medium text-foreground hover:underline block truncate">
                      {project.name}
                    </Link>
                    <p className="mt-0.5 text-xs text-muted-foreground">
                      {new Date(project.deadline).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>
                  <span className={`inline-block whitespace-nowrap rounded-md border px-2 py-0.5 text-[10px] font-semibold ${getUrgencyClasses(daysRemaining)}`}>
                    {getUrgencyLabel(daysRemaining)}
                  </span>
                </div>
                <div>
                  <Select value={project.status} onValueChange={(val) => handleStatusChange(project.id, val)}>
                    <SelectTrigger className="h-7 text-xs w-full bg-background mt-1">
                      <SelectValue placeholder="Status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="active">Active</SelectItem>
                      <SelectItem value="pending">Pending</SelectItem>
                      <SelectItem value="on-hold">On Hold</SelectItem>
                      <SelectItem value="at-risk">At Risk</SelectItem>
                      <SelectItem value="completed">Completed</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
