import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  FolderKanban,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Upload,
  Inbox,
} from "lucide-react";
import { FreelancerPortalService } from "@/api/freelancerPortalService";
import { ProjectResponse } from "@/api/projectService";
import { format, differenceInDays, parseISO } from "date-fns";

const FreelancerDashboard = () => {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchAssignments = async () => {
      try {
        const data = await FreelancerPortalService.getAssignments();
        setProjects(data);
      } catch (err) {
        console.error("Failed to fetch assignments:", err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };
    fetchAssignments();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading your assignments...
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-64 items-center justify-center text-destructive">
        Failed to load assignments. Please try again later.
      </div>
    );
  }

  // Compute KPI counts
  const totalProjects = projects.length;
  const inProgressCount = projects.filter(
    (p) => p.status?.toLowerCase() === "in_progress" || p.status?.toLowerCase() === "in progress"
  ).length;
  const completedCount = projects.filter(
    (p) => p.status?.toLowerCase() === "completed"
  ).length;
  const dueSoonCount = projects.filter((p) => {
    if (!p.deadline) return false;
    const days = differenceInDays(parseISO(p.deadline), new Date());
    return days >= 0 && days <= 7 && p.status?.toLowerCase() !== "completed";
  }).length;

  const kpis = [
    { label: "My Tasks", value: totalProjects, subtitle: "Total assigned", icon: FolderKanban },
    { label: "In Progress", value: inProgressCount, subtitle: "Currently active", icon: Clock },
    { label: "Completed", value: completedCount, subtitle: "Finished", icon: CheckCircle2 },
    { label: "Due Soon", value: dueSoonCount, subtitle: "Within 7 days", icon: AlertTriangle },
  ];

  // Status badge styling
  const getStatusBadge = (status: string) => {
    const s = status?.toLowerCase() || "";
    if (s === "completed") {
      return (
        <span className="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-200">
          Completed
        </span>
      );
    }
    if (s === "in_progress" || s === "in progress") {
      return (
        <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-200">
          In Progress
        </span>
      );
    }
    // Default: Not Started / Pending
    return (
      <span className="inline-flex items-center rounded-full bg-gray-50 px-2.5 py-0.5 text-xs font-medium text-gray-600 ring-1 ring-inset ring-gray-200">
        {status || "Not Started"}
      </span>
    );
  };

  // Deadline urgency
  const getDeadlineDisplay = (deadline: string | null | undefined) => {
    if (!deadline) return <span className="text-xs text-muted-foreground">No deadline</span>;

    const deadlineDate = parseISO(deadline);
    const daysLeft = differenceInDays(deadlineDate, new Date());
    const formatted = format(deadlineDate, "MMM d, yyyy");

    if (daysLeft < 0) {
      return (
        <div className="flex items-center gap-1.5">
          <span className="h-2 w-2 rounded-full bg-red-500" />
          <span className="text-xs font-medium text-red-600">{formatted}</span>
          <span className="text-xs text-red-500">Overdue</span>
        </div>
      );
    }
    if (daysLeft <= 3) {
      return (
        <div className="flex items-center gap-1.5">
          <span className="h-2 w-2 rounded-full bg-red-500" />
          <span className="text-xs font-medium text-red-600">{formatted}</span>
          <span className="text-xs text-red-500">{daysLeft}d left</span>
        </div>
      );
    }
    if (daysLeft <= 7) {
      return (
        <div className="flex items-center gap-1.5">
          <span className="h-2 w-2 rounded-full bg-amber-500" />
          <span className="text-xs font-medium text-amber-600">{formatted}</span>
          <span className="text-xs text-amber-500">{daysLeft}d left</span>
        </div>
      );
    }
    return <span className="text-xs text-muted-foreground">{formatted}</span>;
  };

  // Empty state
  if (totalProjects === 0) {
    return (
      <div className="mx-auto max-w-6xl space-y-6">
        {/* KPI Cards still show zeros */}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {kpis.map((kpi) => (
            <div key={kpi.label} className="rounded-xl border bg-card p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">{kpi.label}</span>
                <kpi.icon className="h-4 w-4 text-muted-foreground" />
              </div>
              <p className="mt-2 text-2xl font-semibold text-foreground">{kpi.value}</p>
              <p className="mt-1 text-xs text-muted-foreground">{kpi.subtitle}</p>
            </div>
          ))}
        </div>

        {/* Empty state card */}
        <div className="rounded-xl border bg-card p-12 shadow-sm">
          <div className="flex flex-col items-center justify-center text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-cream">
              <Inbox className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="mt-4 text-lg font-medium text-foreground">
              No projects assigned yet
            </h3>
            <p className="mt-2 max-w-sm text-sm text-muted-foreground">
              Your manager hasn't assigned any projects to you yet. Once you're added to a project, it will appear here.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      {/* KPI Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpis.map((kpi) => (
          <div key={kpi.label} className="rounded-xl border bg-card p-5 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">{kpi.label}</span>
              <kpi.icon className="h-4 w-4 text-muted-foreground" />
            </div>
            <p className="mt-2 text-2xl font-semibold text-foreground">{kpi.value}</p>
            <p className="mt-1 text-xs text-muted-foreground">{kpi.subtitle}</p>
          </div>
        ))}
      </div>

      {/* Due Soon Section */}
      {dueSoonCount > 0 && (
        <div className="rounded-xl border border-amber-200 bg-amber-50/50 p-5 shadow-sm">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-600" />
            <h3 className="text-sm font-medium text-amber-800">Upcoming Deadlines</h3>
          </div>
          <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {projects
              .filter((p) => {
                if (!p.deadline) return false;
                const days = differenceInDays(parseISO(p.deadline), new Date());
                return days >= 0 && days <= 7 && p.status?.toLowerCase() !== "completed";
              })
              .map((project) => (
                <Link
                  key={project.id}
                  to={`/freelancer/projects/${project.id}`}
                  className="rounded-lg border border-amber-200 bg-white p-3 transition-colors hover:border-amber-300"
                >
                  <p className="text-sm font-medium text-foreground">{project.name}</p>
                  <div className="mt-1">{getDeadlineDisplay(project.deadline)}</div>
                </Link>
              ))}
          </div>
        </div>
      )}

      {/* Assigned Projects Table */}
      <div className="rounded-xl border bg-card shadow-sm">
        <div className="flex items-center justify-between border-b px-5 py-4">
          <div>
            <h3 className="text-sm font-medium text-foreground">Assigned Projects</h3>
            <p className="mt-0.5 text-xs text-muted-foreground">
              {totalProjects} project{totalProjects !== 1 ? "s" : ""} assigned to you
            </p>
          </div>
        </div>

        {/* Desktop table */}
        <div className="hidden md:block">
          <table className="w-full">
            <thead>
              <tr className="border-b text-left">
                <th className="px-5 py-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Project
                </th>
                <th className="px-5 py-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Type
                </th>
                <th className="px-5 py-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Deadline
                </th>
                <th className="px-5 py-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Status
                </th>
                <th className="px-5 py-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {projects.map((project) => (
                <tr key={project.id} className="transition-colors hover:bg-cream/50">
                  <td className="px-5 py-4">
                    <Link
                      to={`/freelancer/projects/${project.id}`}
                      className="text-sm font-medium text-foreground hover:underline"
                    >
                      {project.name}
                    </Link>
                    {project.description && (
                      <p className="mt-0.5 text-xs text-muted-foreground line-clamp-1">
                        {project.description}
                      </p>
                    )}
                  </td>
                  <td className="px-5 py-4">
                    <span className="text-sm text-muted-foreground">
                      {project.type || "—"}
                    </span>
                  </td>
                  <td className="px-5 py-4">{getDeadlineDisplay(project.deadline)}</td>
                  <td className="px-5 py-4">{getStatusBadge(project.status)}</td>
                  <td className="px-5 py-4">
                    <Button variant="outline" size="sm" asChild>
                      <Link to={`/freelancer/projects/${project.id}`}>
                        <Upload className="mr-1.5 h-3.5 w-3.5" />
                        Upload Work
                      </Link>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Mobile cards */}
        <div className="space-y-3 p-4 md:hidden">
          {projects.map((project) => (
            <Link
              key={project.id}
              to={`/freelancer/projects/${project.id}`}
              className="block rounded-lg border bg-cream/30 p-4 transition-colors hover:border-foreground/20"
            >
              <div className="flex items-start justify-between">
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-foreground">{project.name}</p>
                  <p className="mt-0.5 text-xs text-muted-foreground">{project.type || "Project"}</p>
                </div>
                {getStatusBadge(project.status)}
              </div>
              <div className="mt-3 flex items-center justify-between">
                {getDeadlineDisplay(project.deadline)}
                <Button variant="outline" size="sm" className="h-7 text-xs">
                  <Upload className="mr-1 h-3 w-3" />
                  Upload
                </Button>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
};

export default FreelancerDashboard;
