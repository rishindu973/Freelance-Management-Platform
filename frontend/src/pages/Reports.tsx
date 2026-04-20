import { useState, useEffect } from "react";
import { ReportService, ReportResponse } from "@/api/reportService";
import { DollarSign, FolderPlus, CheckCircle2, FileText, Download, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/components/ui/use-toast";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function Reports() {
    const [data, setData] = useState<ReportResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const { toast } = useToast();
    const [isDownloading, setIsDownloading] = useState(false);
    const [downloadProgress, setDownloadProgress] = useState(0);

    // Default to roughly last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(today.getDate() - 30);

    const [startDate, setStartDate] = useState(thirtyDaysAgo.toISOString().split("T")[0]);
    const [endDate, setEndDate] = useState(today.toISOString().split("T")[0]);

    const fetchReport = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await ReportService.getReport(startDate, endDate);
            setData(res);
        } catch (err: any) {
            console.error(err);
            setError("Failed to fetch report data.");
        } finally {
            setLoading(false);
        }
    };

    const handleDownloadPdf = async () => {
        setIsDownloading(true);
        setDownloadProgress(0);
        try {
            const { blob, filename: serverFilename } = await ReportService.downloadReportPdf(
                startDate,
                endDate,
                (progress) => setDownloadProgress(progress)
            );
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;

            const finalFilename = serverFilename || `Report_${startDate}_to_${endDate}.pdf`;

            link.setAttribute('download', finalFilename);
            document.body.appendChild(link);
            link.click();

            link.parentNode?.removeChild(link);
            window.URL.revokeObjectURL(url);

            toast({
                title: "Download Started",
                description: `Your file "${finalFilename}" is being saved.`,
            });
        } catch (error: any) {
            console.error("PDF Download Error:", error);
            toast({
                title: "Download Error",
                description: "Failed to generate PDF. Please try again later.",
                variant: "destructive"
            });
        } finally {
            setIsDownloading(false);
            setDownloadProgress(0);
        }
    };

    useEffect(() => {
        // Only fetch if dates are valid
        if (startDate && endDate) {
            fetchReport();
        }
    }, [startDate, endDate]);

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center p-12 text-destructive">
                <p className="font-semibold text-lg">Error</p>
                <p>{error}</p>
            </div>
        );
    }

    const kpis = [
        { label: "Total Revenue", value: `$${data?.totalRevenue?.toLocaleString(undefined, { minimumFractionDigits: 2 }) ?? "0"}`, icon: DollarSign },
        { label: "Projects Started", value: data?.projectsStarted ?? 0, icon: FolderPlus },
        { label: "Projects Completed", value: data?.projectsCompleted ?? 0, icon: CheckCircle2 },
        { label: "Invoices Generated", value: data?.invoicesGenerated ?? 0, icon: FileText },
    ];

    return (
        <div className="mx-auto max-w-6xl space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-semibold text-foreground">Custom Date Range Reports</h1>
                    <p className="text-sm text-muted-foreground">Select a timeline to view granular reporting metrics.</p>
                </div>
                <div>
                    <Button
                        variant="default"
                        className="gap-2 relative overflow-hidden bg-primary hover:bg-primary/90"
                        onClick={handleDownloadPdf}
                        disabled={isDownloading || loading || !!error}
                    >
                        {isDownloading ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin" />
                                <span>{downloadProgress > 0 ? `${downloadProgress}%` : 'Preparing...'}</span>
                                {downloadProgress > 0 && (
                                    <Progress
                                        value={downloadProgress}
                                        className="absolute bottom-0 left-0 right-0 h-0.5 w-full rounded-none"
                                    />
                                )}
                            </>
                        ) : (
                            <>
                                <Download className="h-4 w-4" />
                                <span>Download PDF</span>
                            </>
                        )}
                    </Button>
                </div>
            </div>

            <div className="flex flex-col sm:flex-row gap-4 items-end rounded-lg border bg-card p-4">
                <div className="w-full sm:w-48">
                    <Label className="mb-1.5 block text-xs text-muted-foreground">Start Date</Label>
                    <Input
                        type="date"
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                    />
                </div>
                <div className="w-full sm:w-48">
                    <Label className="mb-1.5 block text-xs text-muted-foreground">End Date</Label>
                    <Input
                        type="date"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div className="flex h-64 items-center justify-center text-muted-foreground">
                    Loading report data...
                </div>
            ) : data ? (
                <>
                    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                        {kpis.map((kpi) => (
                            <div key={kpi.label} className="rounded-xl border bg-card p-5 shadow-sm">
                                <div className="flex items-center justify-between">
                                    <span className="text-sm text-muted-foreground">{kpi.label}</span>
                                    <kpi.icon className="h-4 w-4 text-muted-foreground" />
                                </div>
                                <p className="mt-2 text-2xl font-semibold text-foreground">{kpi.value}</p>
                            </div>
                        ))}
                    </div>

                    <div className="rounded-xl border bg-card p-5 shadow-sm">
                        <h3 className="text-sm font-medium text-foreground">Revenue Collection Timeline</h3>
                        <p className="mt-0.5 mb-6 text-xs text-muted-foreground">Aggregated daily revenue from recorded payments</p>
                        <div className="h-80 w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={data.revenueTimeline}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
                                    <XAxis
                                        dataKey="date"
                                        tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }}
                                        axisLine={false}
                                        tickLine={false}
                                    />
                                    <YAxis
                                        tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }}
                                        axisLine={false}
                                        tickLine={false}
                                        tickFormatter={(v) => `$${v}`}
                                    />
                                    <Tooltip
                                        cursor={{ fill: "hsl(var(--muted))" }}
                                        contentStyle={{
                                            background: "hsl(var(--background))",
                                            border: "1px solid hsl(var(--border))",
                                            borderRadius: "8px",
                                            fontSize: 12,
                                        }}
                                        formatter={(value: number) => [`$${value.toLocaleString()}`, "Revenue"]}
                                        labelFormatter={(label) => `Date: ${label}`}
                                    />
                                    <Bar dataKey="amount" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </>
            ) : null}
        </div>
    );
}
