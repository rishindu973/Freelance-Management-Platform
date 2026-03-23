import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  ArrowLeft,
  Upload,
  Calendar,
  Clock,
  FileText,
  Users,
} from "lucide-react";
import { FreelancerPortalService } from "@/api/freelancerPortalService";
import { ProjectResponse } from "@/api/projectService";
import { format, differenceInDays, parseISO } from "date-fns";
import { toast } from "sonner";

const FreelancerProjectDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadNote, setUploadNote] = useState("");
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    const fetchProject = async () => {
      try {
        const data = await FreelancerPortalService.getAssignments();
        const found = data.find((p) => p.id === Number(id));
        if (found) {
          setProject(found);
        }
      } catch (err) {
        console.error("Failed to fetch project:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProject();
  }, [id]);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error("Please select a file to upload.");
      return;
    }
    setIsUploading(true);
    try {
      // Placeholder: Actual upload API will be integrated in Story 11
      await new Promise((resolve) => setTimeout(resolve, 1500));
      toast.success("Deliverable uploaded successfully!");
      setSelectedFile(null);
      setUploadNote("");
    } catch (err) {
      toast.error("Failed to upload. Please try again.");
    } finally {
      setIsUploading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const s = status?.toLowerCase() || "";
    if (s === "completed") {
      return (
        <span className="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-200">
          Completed
        </span>
      );
    }
    if (s === "in_progress" || s === "in progress") {
      return (
        <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-200">
          In Progress
        </span>
      );
    }
    return (
      <span className="inline-flex items-center rounded-full bg-gray-50 px-2.5 py-0.5 text-xs font-medium text-gray-600 ring-1 ring-inset ring-gray-200">
        {status || "Not Started"}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading project details...
      </div>
    );
  }

  if (!project) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Link
          to="/freelancer/dashboard"
          className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Dashboard
        </Link>
        <div className="rounded-xl border bg-card p-12 text-center shadow-sm">
          <p className="text-sm text-muted-foreground">
            Project not found or you don't have access to this project.
          </p>
        </div>
      </div>
    );
  }

  const deadlineDays = project.deadline
    ? differenceInDays(parseISO(project.deadline), new Date())
    : null;

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Back link */}
      <Link
        to="/freelancer/dashboard"
        className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Dashboard
      </Link>

      {/* Project Header Card */}
      <div className="rounded-xl border bg-card p-6 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h1 className="text-xl font-semibold text-foreground">{project.name}</h1>
            {project.description && (
              <p className="mt-2 text-sm text-muted-foreground leading-relaxed">
                {project.description}
              </p>
            )}
          </div>
          {getStatusBadge(project.status)}
        </div>

        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          <div className="flex items-center gap-3 rounded-lg border bg-cream/50 p-3">
            <FileText className="h-4 w-4 text-muted-foreground" />
            <div>
              <p className="text-xs text-muted-foreground">Type</p>
              <p className="text-sm font-medium text-foreground">
                {project.type || "Not specified"}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3 rounded-lg border bg-cream/50 p-3">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <div>
              <p className="text-xs text-muted-foreground">Start Date</p>
              <p className="text-sm font-medium text-foreground">
                {project.startDate
                  ? format(parseISO(project.startDate), "MMM d, yyyy")
                  : "Not set"}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3 rounded-lg border bg-cream/50 p-3">
            <Clock
              className={`h-4 w-4 ${
                deadlineDays !== null && deadlineDays <= 3
                  ? "text-red-500"
                  : deadlineDays !== null && deadlineDays <= 7
                  ? "text-amber-500"
                  : "text-muted-foreground"
              }`}
            />
            <div>
              <p className="text-xs text-muted-foreground">Deadline</p>
              <p
                className={`text-sm font-medium ${
                  deadlineDays !== null && deadlineDays <= 3
                    ? "text-red-600"
                    : deadlineDays !== null && deadlineDays <= 7
                    ? "text-amber-600"
                    : "text-foreground"
                }`}
              >
                {project.deadline
                  ? format(parseISO(project.deadline), "MMM d, yyyy")
                  : "No deadline"}
                {deadlineDays !== null && deadlineDays >= 0 && (
                  <span className="ml-1 text-xs font-normal text-muted-foreground">
                    ({deadlineDays}d left)
                  </span>
                )}
                {deadlineDays !== null && deadlineDays < 0 && (
                  <span className="ml-1 text-xs font-normal text-red-500">
                    (Overdue)
                  </span>
                )}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Team Members */}
      {project.team && project.team.length > 0 && (
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center gap-2">
            <Users className="h-4 w-4 text-muted-foreground" />
            <h3 className="text-sm font-medium text-foreground">Team Members</h3>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            {project.team.map((member) => (
              <div
                key={member.id}
                className="flex items-center gap-2 rounded-full border bg-cream/50 px-3 py-1.5"
              >
                <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
                  {member.initials}
                </div>
                <span className="text-xs font-medium text-foreground">{member.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Deliverable Section */}
      <div className="rounded-xl border bg-card p-6 shadow-sm">
        <div className="flex items-center gap-2">
          <Upload className="h-4 w-4 text-muted-foreground" />
          <h3 className="text-sm font-medium text-foreground">Upload Deliverable</h3>
        </div>
        <p className="mt-1 text-xs text-muted-foreground">
          Upload your work files for this project. Supported formats: PDF, ZIP, PNG, JPG, DOCX.
        </p>

        <div className="mt-4 space-y-4">
          {/* File Drop Area */}
          <label
            htmlFor="file-upload"
            className="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-border bg-cream/30 px-6 py-8 transition-colors hover:border-foreground/30 hover:bg-cream/50"
          >
            <Upload className="h-8 w-8 text-muted-foreground" />
            <p className="mt-2 text-sm font-medium text-foreground">
              {selectedFile ? selectedFile.name : "Click to select a file"}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              {selectedFile
                ? `${(selectedFile.size / 1024 / 1024).toFixed(2)} MB`
                : "or drag and drop"}
            </p>
            <input
              id="file-upload"
              type="file"
              className="hidden"
              onChange={handleFileSelect}
              accept=".pdf,.zip,.png,.jpg,.jpeg,.docx,.xlsx"
            />
          </label>

          {/* Note */}
          <div>
            <label
              htmlFor="upload-note"
              className="block text-sm font-medium text-foreground"
            >
              Note <span className="text-muted-foreground font-normal">(optional)</span>
            </label>
            <textarea
              id="upload-note"
              rows={3}
              className="mt-1.5 w-full rounded-lg border bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              placeholder="Add a note about this deliverable..."
              value={uploadNote}
              onChange={(e) => setUploadNote(e.target.value)}
            />
          </div>

          <Button
            onClick={handleUpload}
            disabled={!selectedFile || isUploading}
            className="w-full sm:w-auto"
          >
            {isUploading ? (
              "Uploading..."
            ) : (
              <>
                <Upload className="mr-1.5 h-4 w-4" />
                Submit Deliverable
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default FreelancerProjectDetail;
