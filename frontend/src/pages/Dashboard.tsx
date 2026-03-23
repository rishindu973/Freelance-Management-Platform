import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { NearDeadlineProjects } from "@/components/dashboard/NearDeadlineProjects";
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

// Mock data
const kpis = [
  { label: "Monthly Income", value: "$18,400", change: "+12%", icon: DollarSign, positive: true },
  { label: "Monthly Expenses", value: "$6,200", change: "+3%", icon: TrendingDown, positive: false },
  { label: "Profit", value: "$12,200", change: "+18%", icon: TrendingUp, positive: true },
  { label: "Outstanding", value: "$4,800", change: "3 invoices", icon: AlertCircle, positive: false },
];

const revenueData = [
  { month: "Jul", income: 12000, expenses: 5200 },
  { month: "Aug", income: 14500, expenses: 5800 },
  { month: "Sep", income: 13200, expenses: 5400 },
  { month: "Oct", income: 15800, expenses: 6100 },
  { month: "Nov", income: 16400, expenses: 5900 },
  { month: "Dec", income: 18400, expenses: 6200 },
];

const projectStatus = [
  { name: "Active", value: 8, color: "hsl(140, 25%, 48%)" },
  { name: "Completed", value: 12, color: "hsl(36, 14%, 89%)" },
  { name: "On Hold", value: 3, color: "hsl(38, 60%, 55%)" },
  { name: "Overdue", value: 2, color: "hsl(0, 45%, 55%)" },
];

const upcomingDeadlines = [
  { task: "Brand guidelines v2", project: "Acme Rebrand", due: "Mar 3", assignee: "Sarah K." },
  { task: "Landing page mockup", project: "TechStart Website", due: "Mar 5", assignee: "Mike R." },
  { task: "API documentation", project: "FinFlow App", due: "Mar 7", assignee: "Alex P." },
];

const recentJobs = [
  { task: "Logo design final", project: "Bloom Studio", completed: "Feb 28", assignee: "Sarah K." },
  { task: "Mobile wireframes", project: "HealthTrack", completed: "Feb 27", assignee: "Mike R." },
  { task: "User research report", project: "EduLearn", completed: "Feb 26", assignee: "Priya M." },
];

const pendingWork = [
  { task: "Social media assets", project: "Acme Rebrand", priority: "High", assignee: "Sarah K." },
  { task: "Backend integration", project: "FinFlow App", priority: "Medium", assignee: "Alex P." },
  { task: "Content writing", project: "TechStart Website", priority: "Low", assignee: "Jordan L." },
];

const Dashboard = () => {
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
            <p className={`mt-1 text-xs ${kpi.positive ? "text-success" : "text-warning"}`}>
              {kpi.change}
            </p>
          </div>
        ))}
      </div>

      {/* Charts row */}
      <div className="grid gap-4 lg:grid-cols-3">
        {/* Revenue chart */}
        <div className="col-span-2 rounded-xl border bg-card p-5 shadow-sm">
          <h3 className="text-sm font-medium text-foreground">Income vs Expenses</h3>
          <p className="mt-0.5 text-xs text-muted-foreground">Last 6 months</p>
          <div className="mt-4 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={revenueData}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(36, 14%, 89%)" />
                <XAxis dataKey="month" tick={{ fontSize: 12, fill: "hsl(0, 0%, 42%)" }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 12, fill: "hsl(0, 0%, 42%)" }} axisLine={false} tickLine={false} tickFormatter={(v) => `$${v / 1000}k`} />
                <Tooltip
                  contentStyle={{
                    background: "hsl(0, 0%, 100%)",
                    border: "1px solid hsl(36, 14%, 89%)",
                    borderRadius: "8px",
                    fontSize: 12,
                  }}
                  formatter={(value: number) => [`$${value.toLocaleString()}`, undefined]}
                />
                <Line type="monotone" dataKey="income" stroke="hsl(140, 25%, 48%)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="expenses" stroke="hsl(0, 45%, 55%)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Project status pie */}
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <h3 className="text-sm font-medium text-foreground">Project Status</h3>
          <p className="mt-0.5 text-xs text-muted-foreground">25 total projects</p>
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

      {/* Three column sections */}
      <div className="grid gap-4 lg:grid-cols-3">
        {/* Upcoming deadlines */}
        <NearDeadlineProjects />

        {/* Recently finished */}
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="h-4 w-4 text-success" />
            <h3 className="text-sm font-medium text-foreground">Recently Finished</h3>
          </div>
          <div className="mt-4 space-y-3">
            {recentJobs.map((item) => (
              <div key={item.task} className="rounded-lg border bg-cream p-3">
                <p className="text-sm font-medium text-foreground">{item.task}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">{item.project} · {item.assignee}</p>
                <p className="mt-1 text-xs text-success">{item.completed}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Pending work */}
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center gap-2">
            <ListChecks className="h-4 w-4 text-muted-foreground" />
            <h3 className="text-sm font-medium text-foreground">Pending This Month</h3>
          </div>
          <div className="mt-4 space-y-3">
            {pendingWork.map((item) => (
              <div key={item.task} className="rounded-lg border bg-cream p-3">
                <p className="text-sm font-medium text-foreground">{item.task}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">{item.project} · {item.assignee}</p>
                <span className={`mt-1 inline-block rounded-full px-2 py-0.5 text-xs ${
                  item.priority === "High"
                    ? "bg-destructive/10 text-destructive"
                    : item.priority === "Medium"
                    ? "bg-warning/10 text-warning"
                    : "bg-muted text-muted-foreground"
                }`}>
                  {item.priority}
                </span>
              </div>
            ))}
          </div>
        </div>
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
