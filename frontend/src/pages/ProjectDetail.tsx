import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Calendar, DollarSign, Users, Edit2, Save, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { ProjectService, ProjectResponse, ProjectUpdateRequest } from "@/api/projectService";
import { FreelancerService, Freelancer } from "@/api/freelancerService";
import { mockProjects } from "@/data/mockProjects";

const statusStyle: Record<string, string> = {
  active: "bg-success/15 text-success border-success/30",
  completed: "bg-muted text-muted-foreground border-border",
  "on-hold": "bg-warning/15 text-warning border-warning/30",
  "at-risk": "bg-destructive/15 text-destructive border-destructive/30",
  pending: "bg-muted text-muted-foreground border-border",
};

const priorityStyle: Record<string, string> = {
  high: "bg-destructive/15 text-destructive border-destructive/30",
  medium: "bg-warning/15 text-warning border-warning/30",
  low: "bg-muted text-muted-foreground border-border",
};

const taskStatusStyle: Record<string, string> = {
  done: "bg-success/15 text-success border-success/30",
  "in-progress": "bg-warning/15 text-warning border-warning/30",
  todo: "bg-muted text-muted-foreground border-border",
};

const deliverableStatusStyle: Record<string, string> = {
  approved: "bg-success/15 text-success border-success/30",
  pending: "bg-warning/15 text-warning border-warning/30",
  revision: "bg-destructive/15 text-destructive border-destructive/30",
};

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [freelancers, setFreelancers] = useState<Freelancer[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<ProjectUpdateRequest>({});

  const [isEditingTeam, setIsEditingTeam] = useState(false);
  const [selectedFreelancerIds, setSelectedFreelancerIds] = useState<string[]>([]);

  // Fallback to mock data for tasks and deliverables since they aren't in the API yet
  const mockFallback = mockProjects.find((p) => p.id === id) || mockProjects[0];

  useEffect(() => {
    FreelancerService.getAllFreelancers().then(setFreelancers).catch(console.error);
  }, []);

  const fetchProject = async () => {
    setIsLoading(true);
    try {
      if (id) {
        const data = await ProjectService.getProjectById(Number(id));
        setProject(data);
        setFormData({
          name: data.name,
          description: data.description,
          status: data.status,
          type: data.type,
          deadline: data.deadline,
          startDate: data.startDate,
          clientId: data.clientId,
        });
      }
    } catch (error) {
      console.error(error);
      toast({ title: "Error", description: "Could not load project details.", variant: "destructive" });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProject();
  }, [id]);

  const handleSave = async () => {
    try {
      if (id) {
        await ProjectService.updateProject(Number(id), formData);
        toast({ title: "Project updated", description: "Changes saved successfully." });
        setIsEditing(false);
        fetchProject();
      }
    } catch (error) {
      console.error(error);
      toast({ title: "Error", description: "Failed to save changes.", variant: "destructive" });
    }
  };

  const handleSaveTeam = async () => {
    try {
      if (id) {
        const ids = selectedFreelancerIds.map(Number);
        await ProjectService.updateProjectTeam(Number(id), ids);
        toast({ title: "Team updated", description: "Team members assigned successfully." });
        setIsEditingTeam(false);
        fetchProject();
      }
    } catch (error) {
      console.error(error);
      toast({ title: "Error", description: "Failed to update team.", variant: "destructive" });
    }
  };

  if (isLoading) {
    return <div className="py-24 text-center">Loading project...</div>;
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-muted-foreground">
        <p className="mb-4">Project not found.</p>
        <Button variant="outline" onClick={() => navigate("/projects")}>Back to Projects</Button>
      </div>
    );
  }

  const fmt = (d: string) => {
    if (!d) return "N/A";
    return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
  };

  const currentTeam = project.team || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-3 w-full sm:w-2/3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/projects")} className="mt-0.5">
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="w-full">
            {isEditing ? (
              <div className="space-y-3 w-full max-w-lg">
                <Input
                  value={formData.name || ""}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Project Name"
                  className="font-semibold text-lg"
                />
                <select
                  className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                  value={formData.status || ""}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                >
                  <option value="pending">Pending</option>
                  <option value="active">Active</option>
                  <option value="completed">Completed</option>
                  <option value="on-hold">On Hold</option>
                  <option value="at-risk">At Risk</option>
                </select>
                <Textarea
                  value={formData.description || ""}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Description"
                />
              </div>
            ) : (
              <>
                <div className="flex items-center gap-2">
                  <h1 className="text-2xl font-semibold text-foreground">{project.name}</h1>
                  <Badge variant="outline" className={statusStyle[project.status] || ""}>
                    {project.status.replace("-", " ").replace(/\b\w/g, (c) => c.toUpperCase())}
                  </Badge>
                </div>
                <p className="text-sm text-muted-foreground">Client ID: {project.clientId} · {project.description}</p>
              </>
            )}
          </div>
        </div>

        <div className="flex gap-2">
          {isEditing ? (
            <>
              <Button variant="outline" onClick={() => setIsEditing(false)}>
                <X className="mr-2 h-4 w-4" /> Cancel
              </Button>
              <Button onClick={handleSave}>
                <Save className="mr-2 h-4 w-4" /> Save
              </Button>
            </>
          ) : (
            <Button onClick={() => setIsEditing(true)}>
              <Edit2 className="mr-2 h-4 w-4" /> Edit Project
            </Button>
          )}
        </div>
      </div>

      {/* KPI row */}
      <div className="grid gap-4 sm:grid-cols-4">
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><Calendar className="h-4 w-4 text-accent-foreground" /></div>
            <div className="w-full">
              <p className="text-xs text-muted-foreground">Deadline</p>
              {isEditing ? (
                <Input
                  type="date"
                  value={formData.deadline || ""}
                  onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                  className="mt-1 h-8 text-sm"
                />
              ) : (
                <p className="text-sm font-semibold text-foreground">{fmt(project.deadline)}</p>
              )}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><Users className="h-4 w-4 text-accent-foreground" /></div>
            <div><p className="text-xs text-muted-foreground">Team</p><p className="text-sm font-semibold text-foreground">{currentTeam.length} members</p></div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><DollarSign className="h-4 w-4 text-accent-foreground" /></div>
            <div><p className="text-xs text-muted-foreground">Budget</p><p className="text-sm font-semibold text-foreground">${mockFallback.budget.toLocaleString()}</p></div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex flex-col gap-2 p-4">
            <div className="flex items-center justify-between">
              <p className="text-xs text-muted-foreground">Progress</p>
              <span className="text-xs tabular-nums text-muted-foreground">{mockFallback.progress}%</span>
            </div>
            <Progress value={mockFallback.progress} className="h-2" />
            <p className="text-[11px] text-muted-foreground">Budget used: {Math.round((mockFallback.spent / mockFallback.budget) * 100)}% (${mockFallback.spent.toLocaleString()})</p>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="tasks" className="space-y-4">
        <TabsList>
          <TabsTrigger value="tasks">Tasks ({mockFallback.tasks.length})</TabsTrigger>
          <TabsTrigger value="team">Team ({currentTeam.length})</TabsTrigger>
          <TabsTrigger value="deliverables">Deliverables ({mockFallback.deliverables.length})</TabsTrigger>
          <TabsTrigger value="activity">Activity</TabsTrigger>
        </TabsList>

        {/* Tasks */}
        <TabsContent value="tasks">
          <div className="rounded-lg border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Task</TableHead>
                  <TableHead>Assignee</TableHead>
                  <TableHead>Deadline</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {mockFallback.tasks.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell className="font-medium text-foreground">{t.title}</TableCell>
                    <TableCell className="text-muted-foreground">{t.assignee}</TableCell>
                    <TableCell className="text-muted-foreground">{fmt(t.deadline)}</TableCell>
                    <TableCell><Badge variant="outline" className={priorityStyle[t.priority]}>{t.priority}</Badge></TableCell>
                    <TableCell><Badge variant="outline" className={taskStatusStyle[t.status]}>{t.status.replace("-", " ")}</Badge></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </TabsContent>

        {/* Team */}
        <TabsContent value="team">
          <div className="mb-4 flex justify-between items-center bg-card p-3 border rounded-lg">
            <h3 className="text-sm font-medium">Team Members ({currentTeam.length})</h3>
            {isEditingTeam ? (
              <div className="flex gap-2">
                <Button variant="outline" size="sm" onClick={() => setIsEditingTeam(false)}>Cancel</Button>
                <Button size="sm" onClick={handleSaveTeam}>Save Team</Button>
              </div>
            ) : (
              <Button variant="outline" size="sm" onClick={() => {
                setSelectedFreelancerIds(currentTeam.map((t) => String(t.id)));
                setIsEditingTeam(true);
              }}>
                Manage Team
              </Button>
            )}
          </div>

          {isEditingTeam ? (
            <div className="rounded-md border p-4 space-y-4 bg-card">
              <Label>Select Freelancers</Label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-64 overflow-y-auto p-2 border rounded-md">
                {freelancers.length === 0 ? (
                  <p className="text-sm text-muted-foreground p-2">No freelancers available.</p>
                ) : (
                  freelancers.map((f) => {
                    const isSelected = selectedFreelancerIds.includes(String(f.id));
                    return (
                      <label
                        key={f.id}
                        className={`flex items-center space-x-3 p-3 border rounded-md cursor-pointer transition-colors ${isSelected ? 'bg-primary/5 border-primary/30' : 'bg-background hover:bg-accent'}`}
                      >
                        <input
                          type="checkbox"
                          className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                          checked={isSelected}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setSelectedFreelancerIds([...selectedFreelancerIds, String(f.id)]);
                            } else {
                              setSelectedFreelancerIds(selectedFreelancerIds.filter(id => id !== String(f.id)));
                            }
                          }}
                        />
                        <div className="flex flex-col">
                          <span className="text-sm font-medium leading-none">{f.fullName}</span>
                          {f.title && <span className="text-xs text-muted-foreground mt-1">{f.title}</span>}
                        </div>
                      </label>
                    );
                  })
                )}
              </div>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {currentTeam.length === 0 && <p className="text-sm text-muted-foreground p-2">No team members assigned.</p>}
              {currentTeam.map((m) => (
                <Card key={m.id}>
                  <CardContent className="flex items-center gap-3 p-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-xs font-medium text-primary-foreground">
                      {m.initials}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-foreground">{m.name}</p>
                      <p className="text-xs text-muted-foreground">{m.role}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Deliverables */}
        <TabsContent value="deliverables">
          {mockFallback.deliverables.length === 0 ? (
            <p className="py-12 text-center text-sm text-muted-foreground">No deliverables submitted yet.</p>
          ) : (
            <div className="rounded-lg border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Deliverable</TableHead>
                    <TableHead>Submitted by</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {mockFallback.deliverables.map((d) => (
                    <TableRow key={d.id}>
                      <TableCell className="font-medium text-foreground">{d.title}</TableCell>
                      <TableCell className="text-muted-foreground">{d.submittedBy}</TableCell>
                      <TableCell className="text-muted-foreground">{fmt(d.date)}</TableCell>
                      <TableCell><Badge variant="outline" className={deliverableStatusStyle[d.status]}>{d.status}</Badge></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </TabsContent>

        {/* Activity */}
        <TabsContent value="activity">
          <div className="space-y-3">
            {mockFallback.activity.map((a) => (
              <div key={a.id} className="flex items-start gap-3 rounded-lg border bg-card p-3">
                <div className="mt-1 h-2 w-2 shrink-0 rounded-full bg-muted-foreground/40" />
                <div>
                  <p className="text-sm text-foreground">{a.message}</p>
                  <p className="text-xs text-muted-foreground">{fmt(a.date)}</p>
                </div>
              </div>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}