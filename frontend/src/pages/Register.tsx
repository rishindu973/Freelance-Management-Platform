import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useNavigate, Link } from "react-router-dom";
import { apiClient } from "@/api/axiosClient";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";

const registerSchema = z.object({
    fullName: z.string().min(2, { message: "Name must be at least 2 characters." }),
    email: z.string().email({ message: "Invalid email address." }),
    companyName: z.string().min(2, { message: "Company name is required." }),
    contactNumber: z.string().min(10, { message: "Valid phone number required." }),
});

export default function Register() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const form = useForm<z.infer<typeof registerSchema>>({
        resolver: zodResolver(registerSchema),
        defaultValues: { fullName: "", email: "", companyName: "", contactNumber: "" },
    });

    async function onSubmit(values: z.infer<typeof registerSchema>) {
        try {
            setIsLoading(true);
            await apiClient.post("/api/manager/register", values);

            toast.success(
                "Workspace registered successfully! Please check your email (or the backend console) for your auto-generated secure password.",
                { duration: 8000 }
            );
            navigate("/login");
        } catch (error: any) {
            toast.error(
                error.response?.data?.error || "Registration failed. Email might already exist."
            );
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="flex h-screen items-center justify-center bg-gray-50/50 py-10 overflow-y-auto">
            <Card className="w-full max-w-lg shadow-lg border-0 bg-white/80 backdrop-blur-sm">
                <CardHeader className="space-y-1 text-center">
                    <CardTitle className="text-2xl font-bold tracking-tight">Create Workspace</CardTitle>
                    <CardDescription>
                        Register a new manager account to orchestrate your freelance teams.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">

                            <FormField control={form.control} name="fullName" render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Full Name</FormLabel>
                                    <FormControl><Input placeholder="Harry Hart" {...field} /></FormControl>
                                    <FormMessage />
                                </FormItem>
                            )} />

                            <FormField control={form.control} name="email" render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Work Email</FormLabel>
                                    <FormControl><Input placeholder="manager@kingsman.com" {...field} /></FormControl>
                                    <FormMessage />
                                </FormItem>
                            )} />

                            <div className="grid grid-cols-2 gap-4">
                                <FormField control={form.control} name="companyName" render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Company</FormLabel>
                                        <FormControl><Input placeholder="Kingsman Agency" {...field} /></FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )} />

                                <FormField control={form.control} name="contactNumber" render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Phone</FormLabel>
                                        <FormControl><Input placeholder="(555) 000-1234" {...field} /></FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )} />
                            </div>

                            <div className="pt-2">
                                <Button className="w-full font-semibold" type="submit" disabled={isLoading}>
                                    {isLoading ? "Provisioning Workspace..." : "Create Account"}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
                <CardFooter className="flex justify-center text-sm text-gray-500">
                    <div>
                        Already have a workspace?{" "}
                        <Link to="/login" className="text-primary font-medium hover:underline">
                            Sign in
                        </Link>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
}
