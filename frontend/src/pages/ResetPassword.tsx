import { useState } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import apiClient from "@/api/axiosClient";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { AlertCircle } from "lucide-react";

const resetSchema = z.object({
    password: z.string().min(8, { message: "Password must be at least 8 characters long" }),
    confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
});

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const form = useForm<z.infer<typeof resetSchema>>({
        resolver: zodResolver(resetSchema),
        defaultValues: { password: "", confirmPassword: "" },
    });

    if (!token) {
        return (
            <div className="flex h-screen items-center justify-center bg-gray-50/50">
                <Card className="w-full max-w-md shadow-lg border-0 bg-white/80 backdrop-blur-sm text-center">
                    <CardHeader>
                        <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
                        <CardTitle className="text-2xl font-bold">Invalid Link</CardTitle>
                        <CardDescription>Your password reset link is missing the secure token.</CardDescription>
                    </CardHeader>
                    <CardFooter className="justify-center">
                        <Link to="/login" className="text-primary hover:underline font-medium">Return to Login</Link>
                    </CardFooter>
                </Card>
            </div>
        );
    }

    async function onSubmit(values: z.infer<typeof resetSchema>) {
        try {
            setIsLoading(true);
            // Matches backend: POST /api/auth/reset-password
            await apiClient.post("/api/auth/reset-password", {
                token: token,
                newPassword: values.password
            });
            toast.success("Password successfully reset!");
            navigate("/login", { replace: true });
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Link expired or invalid.");
        } finally { setIsLoading(false); }
    }

    return (
        <div className="flex h-screen items-center justify-center bg-gray-50/50">
            <Card className="w-full max-w-md shadow-lg border-0 bg-white/80 backdrop-blur-sm">
                <CardHeader className="text-center">
                    <CardTitle className="text-2xl font-bold">Set New Password</CardTitle>
                    <CardDescription>Create a strong password for your account.</CardDescription>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField control={form.control} name="password" render={({ field }) => (
                                <FormItem>
                                    <FormLabel>New Password</FormLabel>
                                    <FormControl><Input type="password" placeholder="••••••••" {...field} /></FormControl>
                                    <FormMessage />
                                </FormItem>
                            )} />
                            <FormField control={form.control} name="confirmPassword" render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Confirm Password</FormLabel>
                                    <FormControl><Input type="password" placeholder="••••••••" {...field} /></FormControl>
                                    <FormMessage />
                                </FormItem>
                            )} />
                            <Button className="w-full mt-4" type="submit" disabled={isLoading}>
                                {isLoading ? "Updating..." : "Reset Password"}
                            </Button>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}