import { useState, useEffect } from "react";
import { FinanceService, FinanceSummaryResponse } from "@/api/financeService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
    LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
    XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, ReferenceLine
} from "recharts";

export function FinanceCharts() {
    const [period, setPeriod] = useState<"week" | "month" | "year">("month");
    const [data, setData] = useState<FinanceSummaryResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setIsLoading(true);
        FinanceService.getSummary(period)
            .then(setData)
            .catch((err) => setError(err.message || "Failed to load finance data"))
            .finally(() => setIsLoading(false));
    }, [period]);

    if (error) {
        return <div className="text-red-500 text-sm text-center py-4">{error}</div>;
    }

    if (isLoading && !data) {
        return <div className="text-gray-400 text-sm text-center py-4">Loading financial charts...</div>;
    }

    if (!data) return null;

    const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884d8"];

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-lg font-semibold text-gray-900">Financial Summary</h2>
                    <p className="text-sm text-gray-500">Track profit, income, and expenses</p>
                </div>
                <Select value={period} onValueChange={(v: any) => setPeriod(v)}>
                    <SelectTrigger className="w-[180px]">
                        <SelectValue placeholder="Select period" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="week">Past Week</SelectItem>
                        <SelectItem value="month">Past Year (Monthly)</SelectItem>
                        <SelectItem value="year">Past 5 Years</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
                {/* Profit Trend Chart */}
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-medium">Profit Trend</CardTitle>
                        <CardDescription>Overall profit over time</CardDescription>
                    </CardHeader>
                    <CardContent className="h-72">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={data.profitTrend} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="label" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                                <YAxis tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                                <Tooltip
                                    formatter={(value: number) => [`$${value.toLocaleString()}`, 'Profit']}
                                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                                />
                                <ReferenceLine y={0} stroke="#cbd5e1" />
                                <Line type="monotone" dataKey="profit" stroke="#10b981" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                            </LineChart>
                        </ResponsiveContainer>
                    </CardContent>
                </Card>

                {/* Income vs Expenses Chart */}
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-medium">Income vs Expenses</CardTitle>
                        <CardDescription>Comparison of revenue and costs</CardDescription>
                    </CardHeader>
                    <CardContent className="h-72">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={data.incomeExpense} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="label" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                                <YAxis tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                                <Tooltip
                                    formatter={(value: number) => [`$${value.toLocaleString()}`, undefined]}
                                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                                    cursor={{ fill: 'transparent' }}
                                />
                                <Legend wrapperStyle={{ fontSize: '12px' }} />
                                <Bar dataKey="income" name="Income" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                                <Bar dataKey="expense" name="Expense" fill="#f43f5e" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </CardContent>
                </Card>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
                {/* Expense Breakdown Pie Chart */}
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-medium">Expense Breakdown</CardTitle>
                        <CardDescription>Distribution of expenses by category</CardDescription>
                    </CardHeader>
                    <CardContent className="h-72 flex items-center justify-center">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={data.expenseBreakdown}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={90}
                                    paddingAngle={2}
                                    dataKey="amount"
                                    nameKey="category"
                                >
                                    {data.expenseBreakdown.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    formatter={(value: number, name: string, props: any) => {
                                        return [`$${value.toLocaleString()} (${props.payload.percentage.toFixed(1)}%)`, name];
                                    }}
                                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                                />
                                <Legend layout="vertical" verticalAlign="middle" align="right" wrapperStyle={{ fontSize: '12px' }} />
                            </PieChart>
                        </ResponsiveContainer>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
