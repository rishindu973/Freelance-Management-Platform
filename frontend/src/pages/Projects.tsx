<<<<<<< HEAD
import { useState } from "react";
=======
import { useEffect, useState } from "react";
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
import { useNavigate } from "react-router-dom";
import { Search, Filter, FolderKanban, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
<<<<<<< HEAD
=======
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import {
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
<<<<<<< HEAD
import { mockProjects, clientNames, type ProjectStatus } from "@/data/mockProjects";

const statusConfig: Record<ProjectStatus, { label: string; className: string }> = {
=======
import { ProjectService, ProjectResponse, ProjectCreateRequest } from "@/api/projectService";
import { ClientService, Client } from "@/api/clientService";

const statusConfig: Record<string, { label: string; className: string }> = {
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
  active: { label: "Active", className: "bg-success/15 text-success border-success/30" },
  completed: { label: "Completed", className: "bg-muted text-muted-foreground border-border" },
  "on-hold": { label: "On Hold", className: "bg-warning/15 text-warning border-warning/30" },
  "at-risk": { label: "At Risk", className: "bg-destructive/15 text-destructive border-destructive/30" },
<<<<<<< HEAD
=======
  pending: { label: "Pending", className: "bg-muted text-muted-foreground border-border" },
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
};

export default function Projects() {
  const navigate = useNavigate();
<<<<<<< HEAD
=======
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
  const [search, setSearch] = useState("");
  const [clientFilter, setClientFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [criticalOnly, setCriticalOnly] = useState(false);

<<<<<<< HEAD
  const filtered = mockProjects.filter((p) => {
    if (search && !p.name.toLowerCase().includes(search.toLowerCase()) && !p.client.toLowerCase().includes(search.toLowerCase())) return false;
    if (clientFilter !== "all" && p.client !== clientFilter) return false;
    if (statusFilter !== "all" && p.status !== statusFilter) return false;
    if (criticalOnly) {
      const daysLeft = Math.ceil((new Date(p.deadline).getTime() - Date.now()) / 86400000);
      if (daysLeft > 7 || p.status === "completed") return false;
    }
    return true;
  });

=======
  const { toast } = useToast();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newProject, setNewProject] = useState<Partial<ProjectCreateRequest>>({
    name: "",
    description: "",
    type: "Development",
    status: "pending",
  });

  const handleCreateProject = async () => {
    try {
      if (!newProject.name || !newProject.clientId || !newProject.deadline || !newProject.startDate || !newProject.type) {
        toast({ title: "Error", description: "Please fill in all required fields", variant: "destructive" });
        return;
      }
      await ProjectService.createProject(newProject as ProjectCreateRequest);
      toast({ title: "Success", description: "Project created successfully." });
      setIsDialogOpen(false);
      setNewProject({ name: "", description: "", type: "Development", status: "pending" });
      fetchProjects();
    } catch (e) {
      toast({ title: "Error", description: "Failed to create project", variant: "destructive" });
    }
  };

  useEffect(() => {
    ClientService.getAllClients().then(setClients).catch(console.error);
  }, []);

  const fetchProjects = async () => {
    setIsLoading(true);
    try {
      const params: Record<string, any> = {};
      if (search) params.search = search;
      if (clientFilter !== "all") params.clientId = parseInt(clientFilter, 10);
      if (statusFilter !== "all") params.status = statusFilter;
      if (criticalOnly) params.isCritical = true;

      const data = await ProjectService.getAllProjects(params);
      setProjects(data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, [search, clientFilter, statusFilter, criticalOnly]);

  const getClientName = (id: number) => {
    return clients.find(c => c.id === id)?.name || `Client #${id}`;
  };

>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Projects</h1>
<<<<<<< HEAD
          <p className="text-sm text-muted-foreground">{mockProjects.length} total projects</p>
        </div>
        <Button className="gap-2">
          <Plus className="h-4 w-4" /> New Project
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 rounded-lg border bg-card p-4 sm:flex-row sm:items-end">
=======
          <p className="text-sm text-muted-foreground">{projects.length} total projects</p>
        </div>
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" /> New Project
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Project</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2">
                <Label>Client</Label>
                <Select
                  value={newProject.clientId ? String(newProject.clientId) : ""}
                  onValueChange={(v) => setNewProject({ ...newProject, clientId: Number(v) })}
                >
                  <SelectTrigger><SelectValue placeholder="Select client" /></SelectTrigger>
                  <SelectContent>
                    {clients.map((c) => (
                      <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Project Name</Label>
                <Input
                  value={newProject.name}
                  onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
                  placeholder="E.g., Website Redesign"
                />
              </div>
              <div className="space-y-2">
                <Label>Description</Label>
                <Input
                  value={newProject.description}
                  onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
                  placeholder="Short description..."
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Start Date</Label>
                  <Input
                    type="date"
                    value={newProject.startDate || ""}
                    onChange={(e) => setNewProject({ ...newProject, startDate: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Deadline</Label>
                  <Input
                    type="date"
                    value={newProject.deadline || ""}
                    onChange={(e) => setNewProject({ ...newProject, deadline: e.target.value })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Type</Label>
                  <Select
                    value={newProject.type}
                    onValueChange={(v) => setNewProject({ ...newProject, type: v })}
                  >
                    <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Development">Development</SelectItem>
                      <SelectItem value="Design">Design</SelectItem>
                      <SelectItem value="Marketing">Marketing</SelectItem>
                      <SelectItem value="Other">Other</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <Button className="mt-4" onClick={handleCreateProject}>Create Project</Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 rounded-lg border bg-card p-4 sm:flex-row sm:items-end w-full">
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
        <div className="flex-1">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Search</Label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
<<<<<<< HEAD
              placeholder="Search projects or clients…"
=======
              placeholder="Search projects..."
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9"
            />
          </div>
        </div>
        <div className="w-full sm:w-44">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Client</Label>
          <Select value={clientFilter} onValueChange={setClientFilter}>
            <SelectTrigger><SelectValue placeholder="All clients" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All clients</SelectItem>
<<<<<<< HEAD
              {clientNames.map((c) => (
                <SelectItem key={c} value={c}>{c}</SelectItem>
=======
              {clients.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="w-full sm:w-40">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Status</Label>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger><SelectValue placeholder="All statuses" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All statuses</SelectItem>
              <SelectItem value="active">Active</SelectItem>
<<<<<<< HEAD
=======
              <SelectItem value="pending">Pending</SelectItem>
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
              <SelectItem value="completed">Completed</SelectItem>
              <SelectItem value="on-hold">On Hold</SelectItem>
              <SelectItem value="at-risk">At Risk</SelectItem>
            </SelectContent>
          </Select>
        </div>
<<<<<<< HEAD
        <div className="flex items-center gap-2 pb-0.5">
=======
        <div className="flex items-center gap-2 pb-0.5 ml-2">
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
          <Switch id="critical" checked={criticalOnly} onCheckedChange={setCriticalOnly} />
          <Label htmlFor="critical" className="text-xs whitespace-nowrap text-muted-foreground">Critical only</Label>
        </div>
      </div>

      {/* Table */}
      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Project</TableHead>
              <TableHead>Client</TableHead>
              <TableHead>Deadline</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Team</TableHead>
              <TableHead className="w-36">Progress</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
<<<<<<< HEAD
            {filtered.length === 0 ? (
=======
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                  Loading projects...
                </TableCell>
              </TableRow>
            ) : projects.length === 0 ? (
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                  <FolderKanban className="mx-auto mb-2 h-8 w-8 opacity-40" />
                  No projects match your filters.
                </TableCell>
              </TableRow>
            ) : (
<<<<<<< HEAD
              filtered.map((project) => {
                const cfg = statusConfig[project.status];
=======
              projects.map((project) => {
                const cfg = statusConfig[project.status] || statusConfig.pending;
                // Since progress and team aren't in backend yet, mapping to fallback defaults
                const team = (project as any).team || [];
                const progress = (project as any).progress || 0;

>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
                return (
                  <TableRow
                    key={project.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/projects/${project.id}`)}
                  >
                    <TableCell className="font-medium text-foreground">{project.name}</TableCell>
<<<<<<< HEAD
                    <TableCell className="text-muted-foreground">{project.client}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {new Date(project.deadline).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
=======
                    <TableCell className="text-muted-foreground">{getClientName(project.clientId)}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {project.deadline ? new Date(project.deadline).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : "N/A"}
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className={cfg.className}>{cfg.label}</Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex -space-x-2">
<<<<<<< HEAD
                        {project.team.slice(0, 3).map((m) => (
=======
                        {team.length === 0 && <span className="text-xs text-muted-foreground ml-2">Unassigned</span>}
                        {team.slice(0, 3).map((m: any) => (
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
                          <div
                            key={m.initials}
                            className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-card bg-accent text-[10px] font-medium text-accent-foreground"
                            title={m.name}
                          >
                            {m.initials}
                          </div>
                        ))}
<<<<<<< HEAD
                        {project.team.length > 3 && (
                          <div className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-card bg-muted text-[10px] text-muted-foreground">
                            +{project.team.length - 3}
=======
                        {team.length > 3 && (
                          <div className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-card bg-muted text-[10px] text-muted-foreground">
                            +{team.length - 3}
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
<<<<<<< HEAD
                        <Progress value={project.progress} className="h-2 flex-1" />
                        <span className="text-xs tabular-nums text-muted-foreground">{project.progress}%</span>
=======
                        <Progress value={progress} className="h-2 flex-1" />
                        <span className="text-xs tabular-nums text-muted-foreground">{progress}%</span>
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
<<<<<<< HEAD
}
=======
}
>>>>>>> ac7ced9 (scrum 29 - implemented projects UI)
