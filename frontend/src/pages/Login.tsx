import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
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
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";

// Validation schema
const loginSchema = z.object({
    email: z.string().email({ message: "Invalid email address" }),
    password: z.string().min(1, { message: "Password is required" }),
});

export default function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const [isForgotLoading, setIsForgotLoading] = useState(false);
    const [selectedRole, setSelectedRole] = useState<"MANAGER" | "FREELANCER">("MANAGER");

    const form = useForm<z.infer<typeof loginSchema>>({
        resolver: zodResolver(loginSchema),
        defaultValues: { email: "", password: "" },
    });

    async function onSubmit(values: z.infer<typeof loginSchema>) {
        try {
            setIsLoading(true);
            const response = await apiClient.post("/api/auth/login", values);

            const token = response.data.token;
            const roleFromBackend = response.data.role; // e.g., 'MANAGER' or 'FREELANCER' or 'Freelancer'

            if (token && roleFromBackend) {
                // Normalize role to uppercase
                const normalizedRole = roleFromBackend.toUpperCase();

                // Store directly AND via context
                localStorage.setItem('token', token);
                localStorage.setItem('role', normalizedRole);
                login(token, normalizedRole);
                toast.success("Welcome back!");

                if (normalizedRole === 'FREELANCER') {
                    navigate("/freelancer/dashboard", { replace: true });
                } else {
                    navigate("/dashboard", { replace: true });
                }
            } else {
                console.error('[Login] Backend response missing token or role:', response.data);
                throw new Error("Token missing from response");
            }
        } catch (error: any) {
            toast.error(
                error.response?.data?.message || "Invalid Credentials. Please try again."
            );
        } finally {
            setIsLoading(false);
        }
    }

    async function handleForgotPassword() {
        const email = form.getValues("email");
        if (!email || !z.string().email().safeParse(email).success) {
            toast.error("Please enter a valid email address first to reset your password.");
            return;
        }

        try {
            setIsForgotLoading(true);
            await apiClient.post(`/api/auth/forgot-password?email=${encodeURIComponent(email)}`);
            toast.success("Check your email for password reset instructions!");
        } catch (error: any) {
            toast.error(error.response?.data?.error || "Failed to process reset request.");
        } finally {
            setIsForgotLoading(false);
        }
    }

    return (
        <div className="flex h-screen items-center justify-center bg-gray-50/50">
            <Card className="w-full max-w-md shadow-lg border-0 bg-white/80 backdrop-blur-sm">
                <CardHeader className="space-y-1 text-center">
                    <CardTitle className="text-2xl font-bold tracking-tight">Sign in</CardTitle>
                    <CardDescription>
                        Enter your credentials to access your {selectedRole.toLowerCase()} workspace.
                    </CardDescription>
                    <div className="pt-4">
                        <Tabs
                            value={selectedRole}
                            onValueChange={(val) => setSelectedRole(val as any)}
                            className="w-full"
                        >
                            <TabsList className="grid w-full grid-cols-2">
                                <TabsTrigger value="MANAGER">Manager</TabsTrigger>
                                <TabsTrigger value="FREELANCER">Freelancer</TabsTrigger>
                            </TabsList>
                        </Tabs>
                    </div>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField
                                control={form.control}
                                name="email"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Email</FormLabel>
                                        <FormControl>
                                            <Input placeholder={selectedRole === 'MANAGER' ? "manager@kingsman.com" : "freelancer@example.com"} {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="password"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Password</FormLabel>
                                        <FormControl>
                                            <Input type="password" placeholder="••••••••" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <Button className="w-full font-semibold" type="submit" disabled={isLoading}>
                                {isLoading ? "Signing in..." : `Sign in as ${selectedRole.charAt(0) + selectedRole.slice(1).toLowerCase()}`}
                            </Button>
                        </form>
                    </Form>
                </CardContent>
                <CardFooter className="flex flex-col space-y-4 text-center text-sm text-gray-500">
                    <button
                        type="button"
                        onClick={handleForgotPassword}
                        disabled={isForgotLoading}
                        className="text-primary hover:underline hover:text-primary/90 disabled:opacity-50"
                    >
                        {isForgotLoading ? "Sending email..." : "Forgot your password?"}
                    </button>

                    <div>
                        Don't have a workspace yet?{" "}
                        <Link to="/register" className="text-primary font-medium hover:underline">
                            Register here
                        </Link>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
}
