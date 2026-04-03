import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { WorkSummaryService, WorkSummaryResponse } from "@/api/workSummaryService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { CheckCircle2, Clock, TrendingUp, TrendingDown, ArrowRight, AlertCircle, Loader2 } from "lucide-react";
import { format, differenceInDays } from "date-fns";
import { ProjectResponse } from "@/api/projectService";

export function WorkSummaryWidget() {
    const [data, setData] = useState<WorkSummaryResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setIsLoading(true);
        WorkSummaryService.getWorkSummary()
            .then(setData)
            .catch((err) => setError(err.message || "Failed to load work summary"))
            .finally(() => setIsLoading(false));
    }, []);

    if (error) {
        return (
            <Card className="col-span-full xl:col-span-1">
                <CardContent className="flex h-48 items-center justify-center text-sm text-red-500">
                    {error}
                </CardContent>
            </Card>
        );
    }

    if (isLoading && !data) {
        return (
            <Card className="col-span-full xl:col-span-1">
                <CardContent className="flex h-48 items-center justify-center text-sm text-muted-foreground">
                    <Loader2 className="w-5 h-5 mr-2 animate-spin" /> Loading work summary...
                </CardContent>
            </Card>
        );
    }

    if (!data) return null;

    const renderGrowth = (percentage: number) => {
        if (percentage > 0) {
            return (
                <span className="flex items-center text-xs font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded flex-shrink-0">
                    <TrendingUp className="w-3 h-3 mr-1" />
                    +{percentage.toFixed(0)}% vs last month
                </span>
            );
        } else if (percentage < 0) {
            return (
                <span className="flex items-center text-xs font-medium text-rose-600 bg-rose-50 px-2 py-0.5 rounded flex-shrink-0">
                    <TrendingDown className="w-3 h-3 mr-1" />
                    {percentage.toFixed(0)}% vs last month
                </span>
            );
        }
        return <span className="text-xs text-muted-foreground bg-gray-50 px-2 py-0.5 rounded">No change vs last month</span>;
    };

    const isUrgent = (deadline: string | undefined) => {
        if (!deadline) return false;
        const daysLeft = differenceInDays(new Date(deadline), new Date());
        return daysLeft >= 0 && daysLeft <= 3;
    };

    const isOverdue = (deadline: string | undefined) => {
        if (!deadline) return false;
        return differenceInDays(new Date(deadline), new Date()) < 0;
    };

    return (
        <Card className="flex flex-col h-full shadow-sm">
            <CardHeader className="pb-3 border-b border-border/40">
                <CardTitle className="text-base font-semibold">Work Summary</CardTitle>
                <CardDescription className="text-xs">Month-over-month performance overview</CardDescription>
            </CardHeader>
            <CardContent className="p-0 flex-1 flex flex-col">
                <Tabs defaultValue="completed" className="w-full h-full flex flex-col">
                    <TabsList className="grid w-full grid-cols-2 rounded-none border-b border-border/40 bg-transparent p-0 h-auto">
                        <TabsTrigger 
                            value="completed" 
                            className="data-[state=active]:border-b-2 data-[state=active]:border-primary data-[state=active]:shadow-none rounded-none py-3"
                        >
                            <span className="flex items-center gap-2">
                                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                                Completed
                            </span>
                        </TabsTrigger>
                        <TabsTrigger 
                            value="pending" 
                            className="data-[state=active]:border-b-2 data-[state=active]:border-primary data-[state=active]:shadow-none rounded-none py-3"
                        >
                            <span className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-amber-500" />
                                Pending
                            </span>
                        </TabsTrigger>
                    </TabsList>

                    <TabsContent value="completed" className="m-0 p-4 flex-1 flex flex-col">
                        <div className="flex items-end justify-between mb-4 pb-4 border-b border-border/40">
                            <div>
                                <p className="text-sm font-medium text-muted-foreground mb-1">Finished This Month</p>
                                <p className="text-3xl font-bold tracking-tight">{data.completedThisMonth}</p>
                            </div>
                            {renderGrowth(data.completedGrowthPercentage)}
                        </div>

                        <div className="flex-1 overflow-y-auto pr-1 space-y-3">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Recently Completed</p>
                            {data.completedProjectsThisMonth.length > 0 ? (
                                data.completedProjectsThisMonth.map(project => (
                                    <Link key={project.id} to={`/projects/${project.id}`} className="block group">
                                        <div className="p-3 bg-muted/30 rounded-lg border border-border/50 hover:border-primary/30 transition-colors">
                                            <div className="flex justify-between items-start mb-1">
                                                <p className="text-sm font-medium group-hover:text-primary transition-colors">{project.name}</p>
                                            </div>
                                            <div className="flex items-center text-xs text-muted-foreground">
                                                <span>Client ID: {project.clientId || 'None'}</span>
                                            </div>
                                        </div>
                                    </Link>
                                ))
                            ) : (
                                <p className="text-sm text-center text-muted-foreground py-6">No projects completed this month.</p>
                            )}
                        </div>
                    </TabsContent>

                    <TabsContent value="pending" className="m-0 p-4 flex-1 flex flex-col">
                        <div className="flex items-end justify-between mb-4 pb-4 border-b border-border/40">
                            <div>
                                <p className="text-sm font-medium text-muted-foreground mb-1">Ongoing Projects</p>
                                <p className="text-3xl font-bold tracking-tight">{data.pendingThisMonth}</p>
                            </div>
                            {renderGrowth(data.pendingGrowthPercentage)}
                        </div>

                        <div className="flex-1 overflow-y-auto pr-1 space-y-3">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Near Deadline Focus</p>
                            {data.pendingProjectsNearDeadline.length > 0 ? (
                                data.pendingProjectsNearDeadline.map(project => {
                                    const urgent = isUrgent(project.deadline);
                                    const overdue = isOverdue(project.deadline);
                                    
                                    return (
                                        <Link key={project.id} to={`/projects/${project.id}`} className="block group">
                                            <div className={`p-3 rounded-lg border transition-colors ${
                                                overdue 
                                                    ? 'bg-red-50/50 border-red-200 hover:border-red-300' 
                                                    : urgent 
                                                        ? 'bg-amber-50/50 border-amber-200 hover:border-amber-300' 
                                                        : 'bg-muted/30 border-border/50 hover:border-primary/30'
                                            }`}>
                                                <div className="flex justify-between items-start mb-1">
                                                    <p className={`text-sm font-medium group-hover:underline ${overdue ? 'text-rose-700' : urgent ? 'text-amber-700' : 'text-foreground'}`}>
                                                        {project.name}
                                                    </p>
                                                    {overdue ? (
                                                        <Badge variant="destructive" className="text-[10px] px-1.5 py-0">Overdue</Badge>
                                                    ) : urgent ? (
                                                        <Badge variant="outline" className="text-[10px] bg-amber-100 text-amber-800 border-amber-200 px-1.5 py-0">Due Soon</Badge>
                                                    ) : null}
                                                </div>
                                                <div className="flex items-center text-xs text-muted-foreground space-x-2">
                                                    <span className="flex items-center">
                                                        <Clock className="w-3 h-3 mr-1" /> 
                                                        {project.deadline ? format(new Date(project.deadline), 'MMM d, yyyy') : 'No deadline'}
                                                    </span>
                                                </div>
                                            </div>
                                        </Link>
                                    );
                                })
                            ) : (
                                <p className="text-sm text-center text-muted-foreground py-6">No pending work right now.</p>
                            )}
                        </div>
                    </TabsContent>
                </Tabs>
            </CardContent>
            <div className="p-3 border-t border-border/40 bg-muted/10 text-center">
                <Link to="/projects" className="text-xs text-primary hover:underline font-medium inline-flex items-center">
                    View all projects <ArrowRight className="w-3 h-3 ml-1" />
                </Link>
            </div>
        </Card>
    );
}
