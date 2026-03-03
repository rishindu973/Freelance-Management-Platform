import { useEffect, useState } from "react";
import { Plus, Trash2, Edit, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
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

export default function Freelancers() {
    const [freelancers, setFreelancers] = useState<Freelancer[]>([]);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedFreelancer, setSelectedFreelancer] = useState<Freelancer | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const { toast } = useToast();

    const loadFreelancers = async () => {
        setIsLoading(true);
        try {
            const data = await FreelancerService.getAllFreelancers();
            // Adjust data mapping if needed, dependent on specific backend payload changes
            setFreelancers(data);
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
        loadFreelancers();
    }, []);

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
                // Create
                freelancerData.password = "defaultpass123"; // Provide a default password as dictated by backend
                const newFreelancer = await FreelancerService.createFreelancer(freelancerData);
                setFreelancers([...freelancers, newFreelancer]);
                toast({
                    title: "Freelancer added",
                    description: `${newFreelancer.fullName} has been successfully added.`,
                });
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
                    <h1 className="text-3xl font-bold tracking-tight text-foreground">Freelancers</h1>
                    <p className="text-muted-foreground">Manage your team of freelancers and their details.</p>
                </div>
                <Button onClick={handleOpenCreateDialog}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Freelancer
                </Button>
            </div>

            <div className="rounded-xl border bg-card shadow-sm">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Title</TableHead>
                            <TableHead>Contact</TableHead>
                            <TableHead>Drive Link</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
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
                            freelancers.map((freelancer) => (
                                <TableRow key={freelancer.id}>
                                    <TableCell className="font-medium">
                                        <div>{freelancer.fullName}</div>
                                        <div className="text-xs text-muted-foreground">{freelancer.email}</div>
                                    </TableCell>
                                    <TableCell>{freelancer.title}</TableCell>
                                    <TableCell>{freelancer.contactNumber}</TableCell>
                                    <TableCell>
                                        {freelancer.driveLink ? (
                                            <a
                                                href={freelancer.driveLink.startsWith('http') ? freelancer.driveLink : `https://${freelancer.driveLink}`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="text-primary hover:underline flex items-center gap-1"
                                            >
                                                <ExternalLink className="h-3 w-3" /> Link
                                            </a>
                                        ) : (
                                            <span className="text-muted-foreground text-sm">N/A</span>
                                        )}
                                    </TableCell>
                                    <TableCell className="text-right whitespace-nowrap">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleOpenEditDialog(freelancer)}
                                            className="text-muted-foreground hover:text-foreground"
                                        >
                                            <Edit className="h-4 w-4" />
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
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            <FreelancerFormDialog
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                onSubmit={handleSaveFreelancer}
                initialData={selectedFreelancer}
            />
        </div>
    );
}
