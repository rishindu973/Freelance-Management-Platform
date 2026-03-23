import { useEffect, useState } from "react";
import { ActivityService, ActivityResponse } from "@/data/activityService";
import { Activity, UserPlus, FolderPlus, FileText, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";

export function ActivityFeed() {
  const [activities, setActivities] = useState<ActivityResponse[]>([]);
  const [loading, setLoading] = useState(true);

  // Filter States
  const [typeFilter, setTypeFilter] = useState<string>("");
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);

  // Reset page to 0 when filters change functionally inside the main effect, OR just separate effect
  useEffect(() => {
    setPage(0);
  }, [typeFilter, startDate, endDate]);

  useEffect(() => {
    let active = true;

    const fetchActivities = async () => {
      setLoading(true);
      try {
        const params: Record<string, any> = { size: 10, page };
        if (typeFilter) params.type = typeFilter;
        if (startDate) params.startDate = `${startDate}T00:00:00`;
        if (endDate) params.endDate = `${endDate}T23:59:59`;

        const data = await ActivityService.getActivities(params);
        if (active) {
          setActivities(data.content || []);
          setTotalPages(data.totalPages || 0);
        }
      } catch (error) {
        if (active) console.error("Failed to load activities", error);
      } finally {
        if (active) setLoading(false);
      }
    };

    // Standard debounced fetch to prevent unnecessary re-renders during fast inputs
    const timeoutId = setTimeout(() => {
      fetchActivities();
    }, 300);

    return () => {
      active = false;
      clearTimeout(timeoutId);
    };
  }, [typeFilter, startDate, endDate, page]);

  const getIcon = (type: string) => {
    switch (type) {
      case "MEMBER_ADDED":
        return <UserPlus className="mt-0.5 h-4 w-4 shrink-0 text-blue-500" />;
      case "PROJECT_CREATED":
        return <FolderPlus className="mt-0.5 h-4 w-4 shrink-0 text-success" />;
      case "INVOICE_SENT":
        return <FileText className="mt-0.5 h-4 w-4 shrink-0 text-warning" />;
      default:
        return <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />;
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit'
    }).format(date);
  };

  return (
    <div className="rounded-xl border bg-card p-5 shadow-sm flex flex-col flex-1 h-full min-h-0">
      <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-2">
          <Activity className="h-4 w-4 text-primary" />
          <h3 className="text-sm font-medium text-foreground">Recent Activity</h3>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-2 text-foreground">
          <select 
            className="h-8 max-w-[110px] sm:max-w-[140px] rounded-md border border-input bg-background px-2 text-xs shadow-sm focus:outline-none focus:ring-1 focus:ring-ring disabled:opacity-50"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <option value="">All Types</option>
            <option value="PROJECT_CREATED">Project Created</option>
            <option value="MEMBER_ADDED">Member Added</option>
            <option value="INVOICE_SENT">Invoice Sent</option>
          </select>

          <div className="flex items-center gap-1">
            <input 
              type="date" 
              className="h-8 w-[100px] sm:w-[120px] rounded-md border border-input bg-background px-1.5 sm:px-2 text-xs shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
            <span className="text-muted-foreground text-xs font-medium">-</span>
            <input 
              type="date" 
              className="h-8 w-[100px] sm:w-[120px] rounded-md border border-input bg-background px-1.5 sm:px-2 text-xs shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
        </div>
      </div>
      
      <div className="space-y-3 overflow-y-auto pr-2">
        {loading ? (
          <div className="h-20 animate-pulse rounded-lg bg-cream flex items-center justify-center">
            <p className="text-xs text-muted-foreground">Loading activities...</p>
          </div>
        ) : activities.length === 0 ? (
          <div className="rounded-lg border bg-cream p-4 text-center">
            <p className="text-xs text-muted-foreground">No recent activity found.</p>
          </div>
        ) : (
          activities.map((item) => (
            <div key={item.id} className="flex gap-3 flex-row rounded-lg border bg-cream p-3">
              {getIcon(item.type)}
              <div className="flex flex-col">
                <p className="text-sm font-medium text-foreground leading-tight">{item.description}</p>
                <p className="mt-1.5 text-xs text-muted-foreground">{formatDate(item.timestamp)}</p>
              </div>
            </div>
          ))
        )}
      </div>

      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between border-t pt-4">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0 || loading}
          >
            Previous
          </Button>
          <span className="text-xs text-muted-foreground font-medium">
            Page {page + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1 || loading}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
