import { useEffect, useState } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import apiClient from "@/api/axiosClient";
import { Card, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";

export default function VerifyEmail() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
    const navigate = useNavigate();

    useEffect(() => {
        if (!token) {
            setStatus("error");
            return;
        }

        const verify = async () => {
            try {
                // Matches backend: POST /api/auth/verify?token=...
                await apiClient.post(`/api/auth/verify?token=${token}`);
                setStatus("success");
                setTimeout(() => navigate("/login"), 3000);
            } catch (err) {
                setStatus("error");
            }
        };

        verify();
    }, [token, navigate]);

    return (
        <div className="flex h-screen items-center justify-center bg-gray-50/50 p-4">
            <Card className="w-full max-w-md text-center shadow-lg border-0 bg-white/80 backdrop-blur-sm">
                <CardHeader>
                    {status === "loading" && <Loader2 className="h-12 w-12 animate-spin text-primary mx-auto mb-4" />}
                    {status === "success" && <CheckCircle2 className="h-12 w-12 text-green-500 mx-auto mb-4" />}
                    {status === "error" && <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />}
                    
                    <CardTitle className="text-2xl font-bold">
                        {status === "loading" ? "Verifying..." : status === "success" ? "Verified!" : "Verification Failed"}
                    </CardTitle>
                    <CardDescription>
                        {status === "loading" ? "Please wait while we confirm your account." : 
                         status === "success" ? "Your email is verified. Redirecting to login..." : 
                         "The link is invalid or has already been used."}
                    </CardDescription>
                </CardHeader>
                <CardFooter className="justify-center">
                    <Link to="/login" className="text-primary hover:underline font-medium">Back to Login</Link>
                </CardFooter>
            </Card>
        </div>
    );
}