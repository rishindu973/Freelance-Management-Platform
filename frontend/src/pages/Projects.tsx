import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Filter, FolderKanban, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
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
import { mockProjects, clientNames, type ProjectStatus } from "@/data/mockProjects";

const statusConfig: Record<ProjectStatus, { label: string; className: string }> = {
  active: { label: "Active", className: "bg-success/15 text-success border-success/30" },
  completed: { label: "Completed", className: "bg-muted text-muted-foreground border-border" },
  "on-hold": { label: "On Hold", className: "bg-warning/15 text-warning border-warning/30" },
  "at-risk": { label: "At Risk", className: "bg-destructive/15 text-destructive border-destructive/30" },
};

export default function Projects() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [clientFilter, setClientFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [criticalOnly, setCriticalOnly] = useState(false);

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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Projects</h1>
          <p className="text-sm text-muted-foreground">{mockProjects.length} total projects</p>
        </div>
        <Button className="gap-2">
          <Plus className="h-4 w-4" /> New Project
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 rounded-lg border bg-card p-4 sm:flex-row sm:items-end">
        <div className="flex-1">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Search</Label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search projects or clients…"
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
              {clientNames.map((c) => (
                <SelectItem key={c} value={c}>{c}</SelectItem>
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
              <SelectItem value="completed">Completed</SelectItem>
              <SelectItem value="on-hold">On Hold</SelectItem>
              <SelectItem value="at-risk">At Risk</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-center gap-2 pb-0.5">
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
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                  <FolderKanban className="mx-auto mb-2 h-8 w-8 opacity-40" />
                  No projects match your filters.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((project) => {
                const cfg = statusConfig[project.status];
                return (
                  <TableRow
                    key={project.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/projects/${project.id}`)}
                  >
                    <TableCell className="font-medium text-foreground">{project.name}</TableCell>
                    <TableCell className="text-muted-foreground">{project.client}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {new Date(project.deadline).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className={cfg.className}>{cfg.label}</Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex -space-x-2">
                        {project.team.slice(0, 3).map((m) => (
                          <div
                            key={m.initials}
                            className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-card bg-accent text-[10px] font-medium text-accent-foreground"
                            title={m.name}
                          >
                            {m.initials}
                          </div>
                        ))}
                        {project.team.length > 3 && (
                          <div className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-card bg-muted text-[10px] text-muted-foreground">
                            +{project.team.length - 3}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Progress value={project.progress} className="h-2 flex-1" />
                        <span className="text-xs tabular-nums text-muted-foreground">{project.progress}%</span>
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
}
