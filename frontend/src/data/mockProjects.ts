export type ProjectStatus = "active" | "completed" | "on-hold" | "at-risk";

export interface ProjectMember {
  name: string;
  initials: string;
  role: string;
}

export interface ProjectTask {
  id: string;
  title: string;
  assignee: string;
  deadline: string;
  priority: "high" | "medium" | "low";
  status: "todo" | "in-progress" | "done";
}

export interface ProjectDeliverable {
  id: string;
  title: string;
  submittedBy: string;
  date: string;
  status: "pending" | "approved" | "revision";
}

export interface ProjectActivity {
  id: string;
  message: string;
  date: string;
}

export interface Project {
  id: string;
  name: string;
  client: string;
  deadline: string;
  status: ProjectStatus;
  progress: number;
  team: ProjectMember[];
  tasks: ProjectTask[];
  deliverables: ProjectDeliverable[];
  activity: ProjectActivity[];
  description: string;
  budget: number;
  spent: number;
}

export const mockProjects: Project[] = [
  {
    id: "proj-001",
    name: "Brand Redesign",
    client: "Acme Corp",
    deadline: "2026-03-28",
    status: "active",
    progress: 65,
    description: "Complete brand identity overhaul including logo, guidelines, and collateral.",
    budget: 12000,
    spent: 7800,
    team: [
      { name: "Alice Chen", initials: "AC", role: "Lead Designer" },
      { name: "Bob Martin", initials: "BM", role: "Illustrator" },
    ],
    tasks: [
      { id: "t1", title: "Logo concepts", assignee: "Alice Chen", deadline: "2026-03-10", priority: "high", status: "done" },
      { id: "t2", title: "Brand guidelines doc", assignee: "Alice Chen", deadline: "2026-03-18", priority: "high", status: "in-progress" },
      { id: "t3", title: "Social media templates", assignee: "Bob Martin", deadline: "2026-03-25", priority: "medium", status: "todo" },
    ],
    deliverables: [
      { id: "d1", title: "Logo final files", submittedBy: "Alice Chen", date: "2026-03-08", status: "approved" },
      { id: "d2", title: "Brand guide v1", submittedBy: "Alice Chen", date: "2026-03-16", status: "pending" },
    ],
    activity: [
      { id: "a1", message: "Alice submitted brand guide v1 for review", date: "2026-03-16" },
      { id: "a2", message: "Logo files approved by client", date: "2026-03-09" },
      { id: "a3", message: "Project kicked off", date: "2026-02-15" },
    ],
  },
  {
    id: "proj-002",
    name: "E-commerce Platform",
    client: "Bloom & Co",
    deadline: "2026-04-15",
    status: "active",
    progress: 40,
    description: "Full Shopify store build with custom theme and product catalog.",
    budget: 18000,
    spent: 7200,
    team: [
      { name: "Carlos Diaz", initials: "CD", role: "Developer" },
      { name: "Dana Lee", initials: "DL", role: "Designer" },
      { name: "Alice Chen", initials: "AC", role: "PM" },
    ],
    tasks: [
      { id: "t4", title: "Wireframes", assignee: "Dana Lee", deadline: "2026-03-05", priority: "high", status: "done" },
      { id: "t5", title: "Theme development", assignee: "Carlos Diaz", deadline: "2026-03-30", priority: "high", status: "in-progress" },
      { id: "t6", title: "Product data entry", assignee: "Alice Chen", deadline: "2026-04-10", priority: "medium", status: "todo" },
    ],
    deliverables: [
      { id: "d3", title: "Wireframe deck", submittedBy: "Dana Lee", date: "2026-03-04", status: "approved" },
    ],
    activity: [
      { id: "a4", message: "Carlos started theme development", date: "2026-03-06" },
      { id: "a5", message: "Wireframes approved", date: "2026-03-05" },
    ],
  },
  {
    id: "proj-003",
    name: "Annual Report Design",
    client: "Sterling Industries",
    deadline: "2026-03-05",
    status: "at-risk",
    progress: 80,
    description: "40-page annual report layout and infographic design.",
    budget: 8000,
    spent: 7400,
    team: [
      { name: "Bob Martin", initials: "BM", role: "Designer" },
    ],
    tasks: [
      { id: "t7", title: "Layout design", assignee: "Bob Martin", deadline: "2026-02-28", priority: "high", status: "done" },
      { id: "t8", title: "Infographics", assignee: "Bob Martin", deadline: "2026-03-03", priority: "high", status: "in-progress" },
      { id: "t9", title: "Final review", assignee: "Bob Martin", deadline: "2026-03-05", priority: "high", status: "todo" },
    ],
    deliverables: [
      { id: "d4", title: "Layout draft", submittedBy: "Bob Martin", date: "2026-02-27", status: "approved" },
      { id: "d5", title: "Infographic batch 1", submittedBy: "Bob Martin", date: "2026-03-02", status: "revision" },
    ],
    activity: [
      { id: "a6", message: "Revision requested on infographics", date: "2026-03-02" },
      { id: "a7", message: "Layout approved", date: "2026-02-28" },
    ],
  },
  {
    id: "proj-004",
    name: "Mobile App MVP",
    client: "FitTrack",
    deadline: "2026-05-01",
    status: "active",
    progress: 20,
    description: "React Native fitness tracking app with workout logging and progress charts.",
    budget: 25000,
    spent: 5000,
    team: [
      { name: "Carlos Diaz", initials: "CD", role: "Lead Developer" },
      { name: "Dana Lee", initials: "DL", role: "UI Designer" },
    ],
    tasks: [
      { id: "t10", title: "UI mockups", assignee: "Dana Lee", deadline: "2026-03-15", priority: "high", status: "in-progress" },
      { id: "t11", title: "Auth flow", assignee: "Carlos Diaz", deadline: "2026-03-25", priority: "high", status: "todo" },
      { id: "t12", title: "Workout logger", assignee: "Carlos Diaz", deadline: "2026-04-15", priority: "medium", status: "todo" },
    ],
    deliverables: [],
    activity: [
      { id: "a8", message: "Dana started UI mockups", date: "2026-03-01" },
      { id: "a9", message: "Project created", date: "2026-02-28" },
    ],
  },
  {
    id: "proj-005",
    name: "Marketing Website",
    client: "GreenLeaf Organics",
    deadline: "2026-02-20",
    status: "completed",
    progress: 100,
    description: "5-page marketing site with blog integration and contact forms.",
    budget: 6000,
    spent: 5800,
    team: [
      { name: "Alice Chen", initials: "AC", role: "Developer" },
      { name: "Dana Lee", initials: "DL", role: "Designer" },
    ],
    tasks: [
      { id: "t13", title: "Design mockups", assignee: "Dana Lee", deadline: "2026-02-01", priority: "high", status: "done" },
      { id: "t14", title: "Development", assignee: "Alice Chen", deadline: "2026-02-15", priority: "high", status: "done" },
      { id: "t15", title: "QA & launch", assignee: "Alice Chen", deadline: "2026-02-20", priority: "medium", status: "done" },
    ],
    deliverables: [
      { id: "d6", title: "Final site", submittedBy: "Alice Chen", date: "2026-02-19", status: "approved" },
    ],
    activity: [
      { id: "a10", message: "Site launched", date: "2026-02-20" },
      { id: "a11", message: "QA passed", date: "2026-02-19" },
    ],
  },
  {
    id: "proj-006",
    name: "Product Catalog",
    client: "Acme Corp",
    deadline: "2026-04-30",
    status: "on-hold",
    progress: 10,
    description: "Digital product catalog with search and filtering capabilities.",
    budget: 9000,
    spent: 900,
    team: [
      { name: "Bob Martin", initials: "BM", role: "Designer" },
    ],
    tasks: [
      { id: "t16", title: "Requirements gathering", assignee: "Bob Martin", deadline: "2026-03-20", priority: "medium", status: "done" },
      { id: "t17", title: "Initial concepts", assignee: "Bob Martin", deadline: "2026-04-01", priority: "medium", status: "todo" },
    ],
    deliverables: [],
    activity: [
      { id: "a12", message: "Project put on hold — client reviewing scope", date: "2026-03-01" },
    ],
  },
];

export const clientNames = [...new Set(mockProjects.map((p) => p.client))];
