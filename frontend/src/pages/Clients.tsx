import { useEffect, useState } from "react";
import { Plus, Trash2, Pencil } from "lucide-react";
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
import { ClientService, Client } from "@/api/clientService";
import { ClientFormDialog } from "@/components/clients/ClientFormDialog";
import { PaginationComponent } from "@/components/PaginationComponent";

export default function Clients() {
    const [clients, setClients] = useState<Client[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [size] = useState(10);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedClient, setSelectedClient] = useState<Client | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const { toast } = useToast();

    const loadClients = async (currentPage = 0) => {
        setIsLoading(true);
        try {
            const data = await ClientService.getAllClients(currentPage);
            setClients(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
            setPage(data.number);
        } catch (error) {
            console.error(error);
            toast({
                title: "Error loading clients",
                description: "Could not fetch clients from the server.",
                variant: "destructive",
            });
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadClients(page);
    }, [page]);

    const handleOpenAddDialog = () => {
        setSelectedClient(null);
        setIsDialogOpen(true);
    };

    const handleOpenEditDialog = (client: Client) => {
        setSelectedClient(client);
        setIsDialogOpen(true);
    };

    const handleSaveClient = async (clientData: Client) => {
        try {
            if (selectedClient?.id) {
                // Edit existing client
                const updated = await ClientService.updateClient(selectedClient.id, clientData);
                setClients(clients.map((c) => (c.id === updated.id ? updated : c)));
                toast({
                    title: "Client updated",
                    description: `${updated.name} has been successfully updated.`,
                });
            } else {
                // Add new client
                const newClient = await ClientService.addClient(clientData);
                setClients([...clients, newClient]);
                toast({
                    title: "Client added",
                    description: `${newClient.name} has been successfully added.`,
                });
            }
        } catch (error) {
            console.error(error);
            toast({
                title: selectedClient ? "Error updating client" : "Error adding client",
                description: "There was a problem saving the client data.",
                variant: "destructive",
            });
        }
    };

    const handleDeleteClient = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this client?")) return;
        try {
            await ClientService.deleteClient(id);
            setClients(clients.filter((c) => c.id !== id));
            toast({
                title: "Client deleted",
                description: "The client has been successfully removed.",
            });
        } catch (error) {
            console.error(error);
            toast({
                title: "Error deleting client",
                description: "There was a problem removing the client.",
                variant: "destructive",
            });
        }
    };

    return (
        <div className="mx-auto max-w-6xl space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight text-foreground">Clients</h1>
                    <p className="text-muted-foreground">Manage your clients and their contact information.</p>
                </div>
                <Button onClick={handleOpenAddDialog}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Client
                </Button>
            </div>

            <div className="rounded-xl border bg-card shadow-sm">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID</TableHead>
                            <TableHead>Name</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>Phone</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center">
                                    Loading clients...
                                </TableCell>
                            </TableRow>
                        ) : clients.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center text-muted-foreground p-8">
                                    No clients found. Add a new client to get started.
                                </TableCell>
                            </TableRow>
                        ) : (
                            clients.map((client) => (
                                <TableRow key={client.id}>
                                    <TableCell className="font-medium">{client.id}</TableCell>
                                    <TableCell>{client.name}</TableCell>
                                    <TableCell>{client.email}</TableCell>
                                    <TableCell>{client.phone}</TableCell>
                                    <TableCell className="text-right whitespace-nowrap">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleOpenEditDialog(client)}
                                            className="text-muted-foreground hover:text-foreground"
                                        >
                                            <Pencil className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => client.id && handleDeleteClient(client.id)}
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
            {!isLoading && totalPages > 1 && (
                <div className="flex flex-col sm:flex-row items-center justify-between bg-card p-4 border rounded-xl shadow-sm gap-4 mt-6">
                    <p className="text-sm text-muted-foreground whitespace-nowrap">
                        Showing <span className="font-medium text-foreground">{page * size + 1}</span> to <span className="font-medium text-foreground">{Math.min((page + 1) * size, totalElements)}</span> of <span className="font-medium text-foreground">{totalElements}</span> results
                    </p>
                    <div className="w-auto mx-0 mt-0">
                        <PaginationComponent
                            currentPage={page}
                            totalPages={totalPages}
                            onPageChange={setPage}
                        />
                    </div>
                </div>
            )}

            <ClientFormDialog
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                onSubmit={handleSaveClient}
                initialData={selectedClient}
            />
        </div>
    );
}
