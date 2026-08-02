import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { ProjectResponse, ProjectService } from "@/api/projectService";
import { TaskService, TaskDTO } from "@/api/taskService";
import { FreelancerPortalService } from "@/api/freelancerPortalService";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import {
    ArrowLeft, UploadCloud, FileText, Calendar,
    CheckCircle2, Circle, Clock, Briefcase, Users
} from "lucide-react";
import { format, differenceInDays } from "date-fns";

// ----- Style maps -----
const statusStyle: Record<string, string> = {
    active: "bg-success/15 text-success border-success/30",
    completed: "bg-muted text-muted-foreground border-border",
    "on-hold": "bg-warning/15 text-warning border-warning/30",
    "at-risk": "bg-destructive/15 text-destructive border-destructive/30",
    pending: "bg-muted text-muted-foreground border-border",
};

const taskStatusStyle: Record<string, string> = {
    done: "bg-success/15 text-success border-success/30",
    "in-progress": "bg-warning/15 text-warning border-warning/30",
    todo: "bg-muted text-muted-foreground border-border",
};

const priorityStyle: Record<string, string> = {
    high: "bg-destructive/15 text-destructive border-destructive/30",
    medium: "bg-warning/15 text-warning border-warning/30",
    low: "bg-muted text-muted-foreground border-border",
};

export default function FreelancerProjectDetail() {
    const { id } = useParams();
    const { toast } = useToast();
    const [project, setProject] = useState<ProjectResponse | null>(null);
    const [tasks, setTasks] = useState<TaskDTO[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!id) return;

        Promise.all([
            FreelancerPortalService.getAssignments(),
            TaskService.getTasksByProject(Number(id))
        ])
            .then(([projects, tasksData]) => {
                const found = projects.find(p => p.id === Number(id));
                setProject(found || null);
                setTasks(tasksData || []);
            })
            .catch(console.error)
            .finally(() => setIsLoading(false));
    }, [id]);

    const getDeadlineWarning = (deadlineDate: string) => {
        if (!deadlineDate) return null;
        const daysLeft = differenceInDays(new Date(deadlineDate), new Date());
        if (daysLeft < 0) return { color: "text-red-600 bg-red-50", text: `Overdue by ${Math.abs(daysLeft)} days` };
        if (daysLeft <= 3) return { color: "text-red-600 bg-red-50", text: `${daysLeft} days left` };
        return null;
    };

    const handleTaskStatusChange = async (taskId: number, newStatus: string) => {
        try {
            const updatedTask = await TaskService.updateTaskStatus(taskId, newStatus);
            setTasks(tasks.map(t => t.id === taskId ? updatedTask : t));
        } catch (error) {
            console.error(error);
            toast({ title: "Error", description: "Failed to update task status.", variant: "destructive" });
        }
    };

    if (isLoading) return <div className="p-8 text-center text-gray-500">Loading project details...</div>;
    if (!project) return (
        <div className="p-8 text-center text-red-500">
            <p className="mb-4">Project not found or access denied.</p>
            <Button asChild variant="outline">
                <Link to="/freelancer/projects">Back to Projects</Link>
            </Button>
        </div>
    );

    const deadlineWarning = getDeadlineWarning(project.deadline);
    const progress = project.progressPercentage ?? 0;

    return (
        <div className="max-w-5xl mx-auto space-y-6">
            <div className="flex items-center gap-2">
                <Button variant="ghost" asChild className="-ml-2 text-gray-500">
                    <Link to="/freelancer/projects">
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        Back to Projects
                    </Link>
                </Button>
            </div>

            <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                <div className="space-y-1">
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold text-gray-900">{project.name}</h1>
                        <Badge variant="outline" className={statusStyle[project.status] || ""}>
                            {project.status.toUpperCase()}
                        </Badge>
                    </div>
                    <p className="text-lg text-gray-500">{project.description || "No description provided."}</p>
                </div>

                {deadlineWarning && (
                    <div className={`px-4 py-2 rounded-lg border flex items-center gap-2 ${deadlineWarning.color}`}>
                        <Clock className="w-4 h-4" />
                        <span className="font-semibold">{deadlineWarning.text}</span>
                    </div>
                )}
            </div>

            <div className="grid gap-6 md:grid-cols-3">
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                            <Calendar className="w-4 h-4" /> Deadline
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-xl font-bold">
                            {project.deadline ? format(new Date(project.deadline), 'MMM d, yyyy') : "N/A"}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500 flex items-center gap-2">
                            <Users className="w-4 h-4" /> Team
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-xl font-bold">{project.team?.length || 0} Members</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500 flex items-center justify-between">
                            <span className="flex items-center gap-2"><Briefcase className="w-4 h-4" /> Progress</span>
                            <span>{progress}%</span>
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <Progress value={progress} className="h-2" />
                    </CardContent>
                </Card>
            </div>

            <Tabs defaultValue="overview" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="overview">Overview</TabsTrigger>
                    <TabsTrigger value="tasks">Tasks</TabsTrigger>
                    <TabsTrigger value="deliverables">Deliverables & Uploads</TabsTrigger>
                </TabsList>

                <TabsContent value="overview" className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Project Details</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div>
                                    <p className="text-gray-500">Project Type</p>
                                    <p className="font-medium capitalize">{project.type || "N/A"}</p>
                                </div>
                                {project.clientName && (
                                    <div>
                                        <p className="text-gray-500">Client</p>
                                        <p className="font-medium">{project.clientName}</p>
                                    </div>
                                )}
                                <div>
                                    <p className="text-gray-500">Start Date</p>
                                    <p className="font-medium">
                                        {project.startDate ? format(new Date(project.startDate), 'MMM d, yyyy') : "N/A"}
                                    </p>
                                </div>
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm">Team Members</p>
                                <div className="flex flex-wrap gap-2 mt-2">
                                    {project.team?.map(member => (
                                        <Badge key={member.id} variant="secondary" className="px-3 py-1">
                                            {member.initials} - {member.name} ({member.role})
                                        </Badge>
                                    ))}
                                    {(!project.team || project.team.length === 0) && (
                                        <p className="text-sm text-gray-400 italic">No team members assigned.</p>
                                    )}
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="tasks">
                    <Card>
                        <CardHeader>
                            <CardTitle>Assigned Tasks</CardTitle>
                        </CardHeader>
                        <CardContent>
                                    <p className="text-sm text-gray-500 mb-6 italic">
                                        Update your task status as you make progress.
                                    </p>

                                    {tasks.length === 0 ? (
                                        <div className="text-center py-12 border-2 border-dashed rounded-lg bg-gray-50">
                                            <FileText className="w-12 h-12 text-gray-200 mx-auto mb-4" />
                                            <p className="text-gray-600">No tasks assigned yet.</p>
                                        </div>
                                    ) : (
                                        <div className="rounded-md border bg-white overflow-hidden">
                                            <Table>
                                                <TableHeader className="bg-slate-50/50">
                                                    <TableRow>
                                                        <TableHead>Task</TableHead>
                                                        <TableHead>Deadline</TableHead>
                                                        <TableHead>Priority</TableHead>
                                                        <TableHead>Status</TableHead>
                                                    </TableRow>
                                                </TableHeader>
                                                <TableBody>
                                                    {tasks.map((t) => (
                                                        <TableRow key={t.id}>
                                                            <TableCell className="font-medium text-foreground">{t.title}</TableCell>
                                                            <TableCell className="text-muted-foreground">
                                                                {t.deadline ? format(new Date(t.deadline), 'MMM d, yyyy') : "N/A"}
                                                            </TableCell>
                                                            <TableCell><Badge variant="outline" className={priorityStyle[t.priority]}>{t.priority}</Badge></TableCell>
                                                            <TableCell>
                                                                <Select value={t.status} onValueChange={(v) => handleTaskStatusChange(t.id, v)}>
                                                                    <SelectTrigger className={`w-[130px] h-8 text-xs border ${taskStatusStyle[t.status]}`}>
                                                                        <SelectValue />
                                                                    </SelectTrigger>
                                                                    <SelectContent>
                                                                        <SelectItem value="todo">To Do</SelectItem>
                                                                        <SelectItem value="in-progress">In Progress</SelectItem>
                                                                        <SelectItem value="done">Done</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                            </TableCell>
                                                        </TableRow>
                                                    ))}
                                                </TableBody>
                                            </Table>
                                        </div>
                                    )}
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="deliverables">
                    <div className="grid gap-6 md:grid-cols-2">
                        <Card>
                            <CardHeader>
                                <CardTitle>Upload Deliverable</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="border-2 border-dashed border-gray-200 rounded-lg p-8 text-center hover:bg-gray-50 transition-colors cursor-pointer group">
                                    <UploadCloud className="w-10 h-10 text-gray-400 mx-auto mb-4 group-hover:text-primary transition-colors" />
                                    <p className="text-sm font-medium text-gray-900">Click to upload or drag and drop</p>
                                    <p className="text-xs text-gray-500 mt-1">SVG, PNG, JPG or ZIP (max. 10MB)</p>
                                </div>
                                <Button className="w-full mt-4">
                                    Submit Work
                                </Button>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Submission History</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    <div className="bg-gray-50 rounded p-4 flex items-center justify-between text-sm text-gray-500">
                                        <div className="flex items-center gap-3">
                                            <FileText className="w-5 h-5 text-gray-400" />
                                            <span>No files uploaded yet.</span>
                                        </div>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>
            </Tabs>
        </div>
    );
}
