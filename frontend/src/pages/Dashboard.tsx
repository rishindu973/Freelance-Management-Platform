import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  DollarSign,
  TrendingDown,
  TrendingUp,
  AlertCircle,
  UserPlus,
  FolderPlus,
  FilePlus,
  Clock,
  CheckCircle2,
  ListChecks,
} from "lucide-react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { DashboardService, DashboardResponse } from "@/api/dashboardService";
import { format } from "date-fns";
import { NearDeadlineProjects } from "@/components/dashboard/NearDeadlineProjects";
import { ActivityFeed } from "@/components/dashboard/ActivityFeed";
import { WorkSummaryWidget } from "@/components/dashboard/WorkSummaryWidget";

import { FinanceCharts } from "@/components/dashboard/FinanceCharts";

const Dashboard = () => {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await DashboardService.getDashboard();
        setData(res);
      } catch (err: any) {
        console.error("Failed to fetch dashboard data:", err);
        setError(err.response?.data?.message || err.message || "Unknown error");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <div className="flex h-64 items-center justify-center text-muted-foreground">Loading dashboard data...</div>;
  if (error) return (
    <div className="flex flex-col h-64 items-center justify-center gap-2">
      <p className="text-destructive font-medium">Failed to load dashboard</p>
      <p className="text-xs text-muted-foreground">{error}</p>
    </div>
  );
  if (!data) return <div className="flex h-64 items-center justify-center text-destructive">Dashboard data is empty</div>;

  const kpis = [
    { label: "Total Projects", value: data.totalProjects, change: "All time", icon: FolderPlus, positive: true },
    { label: "Active", value: data.activeProjects, change: "In progress", icon: TrendingUp, positive: true },
    { label: "Completed", value: data.completedProjects, change: "Finished", icon: CheckCircle2, positive: true },
    { label: "Total Income", value: data.totalIncome ? `$${data.totalIncome.toLocaleString()}` : "$0", change: "From paid invoices", icon: DollarSign, positive: true },
  ];

  const projectStatus = [
    { name: "Active", value: data.activeProjects, color: "hsl(140, 25%, 48%)" },
    { name: "Completed", value: data.completedProjects, color: "hsl(36, 14%, 89%)" },
    { name: "Pending", value: data.pendingProjects, color: "hsl(38, 60%, 55%)" },
    { name: "Overdue", value: data.overdueProjects, color: "hsl(0, 45%, 55%)" },
  ].filter(s => s.value > 0);

  const upcomingDeadlines = data.upcomingDeadlines.map((p) => ({
    task: p.name,
    project: p.type || "Project",
    due: p.deadline ? format(new Date(p.deadline), "MMM d") : "No date",
  }));

  const recentJobs = data.recentCompleted.map((p) => ({
    task: p.name,
    project: p.type || "Project",
    completed: p.deadline ? format(new Date(p.deadline), "MMM d") : "No date",
  }));

  const pendingWorkList = data.pendingWork.map((p) => ({
    task: p.name,
    project: p.type || "Project",
    priority: "Normal",
  }));

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
            <p className={`mt-1 text-xs ${kpi.positive ? "text-success" : "text-muted-foreground"}`}>
              {kpi.change}
            </p>
          </div>
        ))}
      </div>

      {/* Finance Charts */}
      <FinanceCharts />

      {/* Project row */}
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="col-span-2">
            <NearDeadlineProjects />
        </div>
        {/* Project status pie */}
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <h3 className="text-sm font-medium text-foreground">Project Status</h3>
          <p className="mt-0.5 text-xs text-muted-foreground">{data.totalProjects} total projects</p>
          <div className="mt-4 h-48">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={projectStatus} cx="50%" cy="50%" innerRadius={50} outerRadius={75} paddingAngle={3} dataKey="value">
                  {projectStatus.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: "hsl(0, 0%, 100%)",
                    border: "1px solid hsl(36, 14%, 89%)",
                    borderRadius: "8px",
                    fontSize: 12,
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-2 flex flex-wrap gap-3">
            {projectStatus.map((s) => (
              <div key={s.name} className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <span className="h-2 w-2 rounded-full" style={{ background: s.color }} />
                {s.name} ({s.value})
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Module rows */}
      <div className="grid gap-4 lg:grid-cols-2 lg:h-[450px]">
        <WorkSummaryWidget />
        <ActivityFeed />
      </div>

      {/* Quick actions */}
      <div className="rounded-xl border bg-card p-5 shadow-sm">
        <h3 className="text-sm font-medium text-foreground">Quick Actions</h3>
        <div className="mt-3 flex flex-wrap gap-3">
          <Button variant="outline" size="sm" asChild>
            <Link to="/clients"><UserPlus className="mr-1.5 h-3.5 w-3.5" /> Add Client</Link>
          </Button>
          <Button variant="outline" size="sm" asChild>
            <Link to="/projects"><FolderPlus className="mr-1.5 h-3.5 w-3.5" /> Add Project</Link>
          </Button>
          <Button variant="outline" size="sm" asChild>
            <Link to="/invoices/new"><FilePlus className="mr-1.5 h-3.5 w-3.5" /> Create Invoice</Link>
          </Button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
