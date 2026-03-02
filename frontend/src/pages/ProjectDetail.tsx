import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Calendar, DollarSign, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { mockProjects, type ProjectStatus } from "@/data/mockProjects";

const statusStyle: Record<ProjectStatus, string> = {
  active: "bg-success/15 text-success border-success/30",
  completed: "bg-muted text-muted-foreground border-border",
  "on-hold": "bg-warning/15 text-warning border-warning/30",
  "at-risk": "bg-destructive/15 text-destructive border-destructive/30",
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
  const project = mockProjects.find((p) => p.id === id);

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-muted-foreground">
        <p className="mb-4">Project not found.</p>
        <Button variant="outline" onClick={() => navigate("/projects")}>Back to Projects</Button>
      </div>
    );
  }

  const fmt = (d: string) => new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
  const budgetPct = Math.round((project.spent / project.budget) * 100);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/projects")} className="mt-0.5">
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-semibold text-foreground">{project.name}</h1>
              <Badge variant="outline" className={statusStyle[project.status]}>
                {project.status.replace("-", " ").replace(/\b\w/g, (c) => c.toUpperCase())}
              </Badge>
            </div>
            <p className="text-sm text-muted-foreground">{project.client} · {project.description}</p>
          </div>
        </div>
      </div>

      {/* KPI row */}
      <div className="grid gap-4 sm:grid-cols-4">
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><Calendar className="h-4 w-4 text-accent-foreground" /></div>
            <div><p className="text-xs text-muted-foreground">Deadline</p><p className="text-sm font-semibold text-foreground">{fmt(project.deadline)}</p></div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><Users className="h-4 w-4 text-accent-foreground" /></div>
            <div><p className="text-xs text-muted-foreground">Team</p><p className="text-sm font-semibold text-foreground">{project.team.length} members</p></div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent"><DollarSign className="h-4 w-4 text-accent-foreground" /></div>
            <div><p className="text-xs text-muted-foreground">Budget</p><p className="text-sm font-semibold text-foreground">${project.budget.toLocaleString()}</p></div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex flex-col gap-2 p-4">
            <div className="flex items-center justify-between">
              <p className="text-xs text-muted-foreground">Progress</p>
              <span className="text-xs tabular-nums text-muted-foreground">{project.progress}%</span>
            </div>
            <Progress value={project.progress} className="h-2" />
            <p className="text-[11px] text-muted-foreground">Budget used: {budgetPct}% (${project.spent.toLocaleString()})</p>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="tasks" className="space-y-4">
        <TabsList>
          <TabsTrigger value="tasks">Tasks ({project.tasks.length})</TabsTrigger>
          <TabsTrigger value="team">Team ({project.team.length})</TabsTrigger>
          <TabsTrigger value="deliverables">Deliverables ({project.deliverables.length})</TabsTrigger>
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
                {project.tasks.map((t) => (
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
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {project.team.map((m) => (
              <Card key={m.initials}>
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
        </TabsContent>

        {/* Deliverables */}
        <TabsContent value="deliverables">
          {project.deliverables.length === 0 ? (
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
                  {project.deliverables.map((d) => (
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
            {project.activity.map((a) => (
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
