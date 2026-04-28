import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Freelancer } from "@/api/freelancerService";

interface FreelancerFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onSubmit: (freelancer: Freelancer) => void;
    initialData?: Freelancer | null;
}

export function FreelancerFormDialog({
    open,
    onOpenChange,
    onSubmit,
    initialData,
}: FreelancerFormDialogProps) {
    const [formData, setFormData] = useState<Partial<Freelancer>>({
        fullName: "",
        email: "",
        title: "",
        contactNumber: "",
        driveLink: "",
        status: "Active",
        role: "FREELANCER",
    });

    useEffect(() => {
        if (initialData) {
            setFormData(initialData);
        } else {
            setFormData({
                fullName: "",
                email: "",
                title: "",
                contactNumber: "",
                driveLink: "",
                status: "Active",
                role: "FREELANCER",
            });
        }
    }, [initialData, open]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.fullName && formData.email) {
            onSubmit(formData as Freelancer);
            onOpenChange(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>{initialData ? "Edit Freelancer" : "Add New Freelancer"}</DialogTitle>
                    <DialogDescription>
                        Enter the details of the freelancer below. Click save when you're done.
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit}>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="fullName" className="text-right whitespace-nowrap">
                                Full Name
                            </Label>
                            <Input
                                id="fullName"
                                name="fullName"
                                value={formData.fullName || ""}
                                onChange={handleChange}
                                className="col-span-3"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="email" className="text-right">
                                Email
                            </Label>
                            <Input
                                id="email"
                                name="email"
                                type="email"
                                value={formData.email || ""}
                                onChange={handleChange}
                                className="col-span-3"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="title" className="text-right">
                                Title
                            </Label>
                            <Input
                                id="title"
                                name="title"
                                value={formData.title || ""}
                                onChange={handleChange}
                                className="col-span-3"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="contactNumber" className="text-right whitespace-nowrap">
                                Contact
                            </Label>
                            <Input
                                id="contactNumber"
                                name="contactNumber"
                                value={formData.contactNumber || ""}
                                onChange={handleChange}
                                className="col-span-3"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="driveLink" className="text-right whitespace-nowrap">
                                Drive Link
                            </Label>
                            <Input
                                id="driveLink"
                                name="driveLink"
                                type="url"
                                value={formData.driveLink || ""}
                                onChange={handleChange}
                                className="col-span-3"
                                required
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                            Cancel
                        </Button>
                        <Button type="submit">Save changes</Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
