import { useEffect, useState } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { apiClient } from "@/api/axiosClient";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";

export default function VerifyEmail() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();

    const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
    const [message, setMessage] = useState("Verifying your email address...");

    useEffect(() => {
        if (!token) {
            setStatus("error");
            setMessage("Invalid verification link. No token provided.");
            return;
        }

        const verifyToken = async () => {
            try {
                // Call the backend verify endpoint
                await apiClient.post(`/api/auth/verify?token=${token}`);
                setStatus("success");
                setMessage("Your email has been successfully verified! You can now log in to your account.");
            } catch (error: any) {
                setStatus("error");
                setMessage(error.response?.data?.message || "Verification failed. The link may be invalid or expired.");
            }
        };

        verifyToken();
    }, [token]);

    return (
        <div className="flex h-screen items-center justify-center bg-gray-50/50">
            <Card className="w-full max-w-md shadow-lg border-0 bg-white/80 backdrop-blur-sm text-center">
                <CardHeader className="space-y-1">
                    <div className="flex justify-center mb-4">
                        {status === "loading" && <Loader2 className="h-12 w-12 text-blue-500 animate-spin" />}
                        {status === "success" && <CheckCircle2 className="h-12 w-12 text-green-500" />}
                        {status === "error" && <XCircle className="h-12 w-12 text-red-500" />}
                    </div>
                    <CardTitle className="text-2xl font-bold tracking-tight">
                        {status === "loading" && "Verifying Email"}
                        {status === "success" && "Email Verified!"}
                        {status === "error" && "Verification Failed"}
                    </CardTitle>
                    <CardDescription className="text-base mt-2">
                        {message}
                    </CardDescription>
                </CardHeader>
                <CardContent>
                </CardContent>
                <CardFooter className="flex flex-col space-y-4">
                    <Button
                        className="w-full font-semibold"
                        onClick={() => navigate("/login")}
                    >
                        Go to Login
                    </Button>
                    <div className="text-sm text-gray-500">
                        Need help? <Link to="/support" className="text-primary hover:underline">Contact Support</Link>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
}
