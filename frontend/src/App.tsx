import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "./pages/Index";
import NotFound from "./pages/NotFound";
import { AppLayout } from "./components/layout/AppLayout";
import Dashboard from "./pages/Dashboard";
import Clients from "./pages/Clients";
import Freelancers from "./pages/Freelancers";
import Projects from "./pages/Projects";
import ProjectDetail from "./pages/ProjectDetail";
import Invoices from "./pages/Invoices";
import Payments from "./pages/Payments";
import Login from "./pages/Login";
import Register from "./pages/Register";
import VerifyEmail from "./pages/VerifyEmail";
import ResetPassword from "./pages/ResetPassword";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedRoute } from "./components/layout/ProtectedRoute";
import { FreelancerProtectedRoute } from "./components/layout/FreelancerProtectedRoute";
import FreelancerLayout from "./components/layout/FreelancerLayout";
import FreelancerDashboard from "./pages/freelancer/FreelancerDashboard";
import FreelancerProjects from "./pages/freelancer/FreelancerProjects";
import FreelancerProjectDetail from "./pages/freelancer/FreelancerProjectDetail";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <AuthProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            {/* Public routes */}
            <Route path="/" element={<Index />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify" element={<VerifyEmail />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            {/* App routes */}
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/clients" element={<Clients />} />
                <Route path="/freelancers" element={<Freelancers />} />
                <Route path="/projects" element={<Projects />} />
                <Route path="/projects/:id" element={<ProjectDetail />} />
                <Route path="/invoices" element={<Invoices />} />
                <Route path="/payments" element={<Payments />} />
              </Route>
            </Route>

            {/* Freelancer routes */}
            <Route element={<FreelancerProtectedRoute />}>
              <Route element={<FreelancerLayout />}>
                <Route path="/freelancer/dashboard" element={<FreelancerDashboard />} />
                <Route path="/freelancer/projects" element={<FreelancerProjects />} />
                <Route path="/freelancer/projects/:id" element={<FreelancerProjectDetail />} />
              </Route>
            </Route>

            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
