import { useEffect, useState, useRef } from "react";
import { Plus, Trash2, Pencil, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useVirtualizer } from "@tanstack/react-virtual";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { useToast } from "@/hooks/use-toast";
import { FreelancerService, Freelancer } from "@/api/freelancerService";
import { FreelancerFormDialog } from "@/components/freelancers/FreelancerFormDialog";
import { PaginationComponent } from "@/components/PaginationComponent";

export default function Freelancers() {
    const [freelancers, setFreelancers] = useState<Freelancer[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [size, setSize] = useState(10);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedFreelancer, setSelectedFreelancer] = useState<Freelancer | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const { toast } = useToast();

    const loadFreelancers = async (currentPage = page, currentSize = size) => {
        setIsLoading(true);
        try {
            const data = await FreelancerService.getAllFreelancers(currentPage, currentSize);
            setFreelancers(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
            setPage(data.number);
        } catch (error) {
            console.error(error);
            toast({
                title: "Error loading freelancers",
                description: "Could not fetch freelancers from the server.",
                variant: "destructive",
            });
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadFreelancers(page, size);
    }, [page, size]);

    const handleSizeChange = (newSize: string) => {
        setSize(Number(newSize));
        setPage(0); // Reset to first page
    };

    const parentRef = useRef<HTMLDivElement>(null);
    const rowVirtualizer = useVirtualizer({
        count: freelancers.length,
        getScrollElement: () => parentRef.current,
        estimateSize: () => 64, // Estimated row height
        overscan: 5,
    });

    const handleOpenCreateDialog = () => {
        setSelectedFreelancer(null);
        setIsDialogOpen(true);
    };

    const handleOpenEditDialog = (freelancer: Freelancer) => {
        setSelectedFreelancer(freelancer);
        setIsDialogOpen(true);
    };

    const handleSaveFreelancer = async (freelancerData: Freelancer) => {
        try {
            if (selectedFreelancer?.id) {
                // Update
                const updated = await FreelancerService.updateFreelancer(selectedFreelancer.id, freelancerData);
                setFreelancers(freelancers.map((f) => (f.id === updated.id ? updated : f)));
                toast({
                    title: "Freelancer updated",
                    description: "The freelancer details have been updated successfully.",
                });
            } else {
                // Create - backend returns TeamResponseDTO (name/email/password), not a Freelancer shape
                // so we re-fetch the full list after creation
                await FreelancerService.createFreelancer(freelancerData);
                toast({
                    title: "Freelancer added",
                    description: `${freelancerData.fullName} has been successfully added. Login credentials have been sent to ${freelancerData.email}.`,
                });
                loadFreelancers(page);
            }
        } catch (error) {
            console.error(error);
            toast({
                title: "Error saving freelancer",
                description: "There was a problem saving the freelancer data.",
                variant: "destructive",
            });
        }
    };

    const handleDeleteFreelancer = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this freelancer?")) return;
        try {
            await FreelancerService.deleteFreelancer(id);
            setFreelancers(freelancers.filter((f) => f.id !== id));
            toast({
                title: "Freelancer deleted",
                description: "The freelancer has been successfully removed.",
            });
        } catch (error) {
            console.error(error);
            toast({
                title: "Error deleting freelancer",
                description: "There was a problem removing the freelancer.",
                variant: "destructive",
            });
        }
    };

    return (
        <div className="mx-auto max-w-6xl space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight text-[#064e3b]">Freelancers</h1>
                    <p className="text-muted-foreground">Manage your team of freelancers and their details.</p>
                </div>
                <Button onClick={handleOpenCreateDialog}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Freelancer
                </Button>
            </div>

            <div ref={parentRef} className="rounded-xl border bg-card shadow-sm h-[550px] overflow-auto">
                <Table className="relative min-w-full table-fixed">
                    <TableHeader className="sticky top-0 bg-card z-10">
                        <TableRow>
                            <TableHead className="w-[30%]">Name</TableHead>
                            <TableHead className="w-[20%]">Title</TableHead>
                            <TableHead className="w-[20%]">Contact</TableHead>
                            <TableHead className="w-[15%]">Drive Link</TableHead>
                            <TableHead className="w-[15%] text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody
                        style={{
                            height: `${rowVirtualizer.getTotalSize()}px`,
                            position: 'relative',
                        }}
                    >
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center">
                                    Loading freelancers...
                                </TableCell>
                            </TableRow>
                        ) : freelancers.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center text-muted-foreground p-8">
                                    No freelancers found. Add a new freelancer to get started.
                                </TableCell>
                            </TableRow>
                        ) : (
                            rowVirtualizer.getVirtualItems().map((virtualRow) => {
                                const freelancer = freelancers[virtualRow.index];
                                return (
                                <TableRow 
                                    key={freelancer.id}
                                    style={{
                                        position: 'absolute',
                                        top: 0,
                                        left: 0,
                                        width: '100%',
                                        height: `${virtualRow.size}px`,
                                        transform: `translateY(${virtualRow.start}px)`,
                                        display: 'table',
                                        tableLayout: 'fixed'
                                    }}
                                >
                                    <TableCell className="font-medium truncate w-[30%]">
                                        <div className="truncate">{freelancer.fullName}</div>
                                        <div className="text-xs text-muted-foreground truncate">{freelancer.email}</div>
                                    </TableCell>
                                    <TableCell className="truncate w-[20%]">{freelancer.title}</TableCell>
                                    <TableCell className="truncate w-[20%]">{freelancer.contactNumber}</TableCell>
                                    <TableCell className="truncate w-[15%]">
                                        {freelancer.driveLink ? (
                                            <a
                                                href={freelancer.driveLink.startsWith('http') ? freelancer.driveLink : `https://${freelancer.driveLink}`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="text-primary hover:underline flex items-center gap-1 truncate"
                                            >
                                                <ExternalLink className="h-3 w-3 shrink-0" /> Link
                                            </a>
                                        ) : (
                                            <span className="text-muted-foreground text-sm">N/A</span>
                                        )}
                                    </TableCell>
                                    <TableCell className="text-right whitespace-nowrap w-[15%]">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleOpenEditDialog(freelancer)}
                                            className="text-muted-foreground hover:text-foreground"
                                        >
                                            <Pencil className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => freelancer.id && handleDeleteFreelancer(freelancer.id)}
                                            className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                                );
                            })
                        )}
                    </TableBody>
                </Table>
            </div>
            {!isLoading && totalPages > 1 && (
                <div className="flex flex-col sm:flex-row items-center justify-between bg-card p-4 border rounded-xl shadow-sm gap-4 mt-6">
                    <div className="flex items-center gap-4">
                        <p className="text-sm text-muted-foreground whitespace-nowrap">
                            Showing <span className="font-medium text-foreground">{page * size + 1}</span> to <span className="font-medium text-foreground">{Math.min((page + 1) * size, totalElements)}</span> of <span className="font-medium text-foreground">{totalElements}</span> results
                        </p>
                        <div className="flex items-center gap-2">
                            <span className="text-sm text-muted-foreground">Rows per page:</span>
                            <Select value={size.toString()} onValueChange={handleSizeChange}>
                                <SelectTrigger className="w-[70px] h-8 text-sm">
                                    <SelectValue placeholder="10" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="10">10</SelectItem>
                                    <SelectItem value="25">25</SelectItem>
                                    <SelectItem value="50">50</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <div className="w-auto mx-0 mt-0">
                        <PaginationComponent
                            currentPage={page}
                            totalPages={totalPages}
                            onPageChange={setPage}
                        />
                    </div>
                </div>
            )}

            <FreelancerFormDialog
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                onSubmit={handleSaveFreelancer}
                initialData={selectedFreelancer}
            />
        </div>
    );
}
