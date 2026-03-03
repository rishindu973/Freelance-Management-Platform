export type ProjectStatus = "active" | "completed" | "on-hold" | "at-risk";
export type Priority = "high" | "medium" | "low";
export type TaskStatus = "todo" | "in-progress" | "done";
export type DeliverableStatus = "pending" | "approved" | "revision";

export interface TeamMember {
    name: string;
    role: string;
    initials: string;
}

export interface ProjectTask {
    id: string;
    title: string;
    assignee: string;
    deadline: string;
    priority: Priority;
    status: TaskStatus;
}

export interface Deliverable {
    id: string;
    title: string;
    submittedBy: string;
    date: string;
    status: DeliverableStatus;
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
    description: string;
    status: ProjectStatus;
    progress: number;
    budget: number;
    spent: number;
    deadline: string;
    team: TeamMember[];
    tasks: ProjectTask[];
    deliverables: Deliverable[];
    activity: ProjectActivity[];
}

export const clientNames = [
    "Acme Corp",
    "Globex Inc",
    "Stark Industries",
    "Wayne Enterprises",
];

export const mockProjects: Project[] = [
    {
        id: "PRJ-001",
        name: "Website Redesign",
        client: "Acme Corp",
        description: "Complete overhaul of the corporate website.",
        status: "active",
        progress: 65,
        budget: 15000,
        spent: 8500,
        deadline: "2023-12-15",
        team: [
            { name: "Alice Smith", role: "Designer", initials: "AS" },
            { name: "Bob Jones", role: "Developer", initials: "BJ" },
        ],
        tasks: [
            {
                id: "T-01",
                title: "Design mockups",
                assignee: "Alice Smith",
                deadline: "2023-11-20",
                priority: "high",
                status: "done",
            },
            {
                id: "T-02",
                title: "Frontend implementation",
                assignee: "Bob Jones",
                deadline: "2023-12-05",
                priority: "high",
                status: "in-progress",
            },
        ],
        deliverables: [
            {
                id: "D-01",
                title: "Figma files",
                submittedBy: "Alice Smith",
                date: "2023-11-21",
                status: "approved",
            },
        ],
        activity: [
            {
                id: "A-01",
                message: "Alice submitted Figma files.",
                date: "2023-11-21T10:00:00Z",
            },
        ],
    },
    {
        id: "PRJ-002",
        name: "Mobile App Development",
        client: "Globex Inc",
        description: "Build iOS and Android apps for the new product.",
        status: "at-risk",
        progress: 30,
        budget: 45000,
        spent: 20000,
        deadline: "2024-02-28",
        team: [
            { name: "Charlie Brown", role: "Mobile Dev", initials: "CB" },
            { name: "Diana Prince", role: "QA Engineer", initials: "DP" },
        ],
        tasks: [
            {
                id: "T-03",
                title: "Setup React Native project",
                assignee: "Charlie Brown",
                deadline: "2023-10-15",
                priority: "high",
                status: "done",
            },
            {
                id: "T-04",
                title: "Implement authentication",
                assignee: "Charlie Brown",
                deadline: "2023-11-10",
                priority: "high",
                status: "todo",
            },
        ],
        deliverables: [],
        activity: [
            {
                id: "A-02",
                message: "Charlie initialized the project.",
                date: "2023-10-15T14:30:00Z",
            },
        ],
    },
];
