const createProjects = async () => {
    try {
        let clientsRes = await fetch("http://localhost:8081/clients/all");
        let clients = await clientsRes.json();

        let clientId;
        if (!Array.isArray(clients) || clients.length === 0) {
            console.log("No clients found, creating dummy client...");
            const newClientRes = await fetch("http://localhost:8081/clients/add", {
                method: "POST",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    companyName: "Acme Corp",
                    pointOfContact: "John Doe",
                    email: "contact@acme.com",
                    status: "Active"
                })
            });
            const newClient = await newClientRes.json();
            clientId = newClient.id;
            console.log("Created client ID:", clientId);
        } else {
            clientId = clients[0].id; // taking the first existing one
        }

        if (!clientId) {
            console.error("clientId is null or undefined! clients response:", clients);
            return;
        }

        const p1 = {
            clientId: clientId,
            name: "Brand Guidelines v2",
            description: "Update the corporate brand guidelines for 2026.",
            type: "Design",
            startDate: new Date().toISOString().split('T')[0],
            deadline: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0], // 5 days from now (critical)
            status: "active"
        };
        const p2 = {
            clientId: clientId,
            name: "Q3 Marketing Campaign",
            description: "Launch the new marketing initiatives.",
            type: "Marketing",
            startDate: new Date(Date.now() - 10 * 86400000).toISOString().split('T')[0],
            deadline: new Date(Date.now() + 20 * 86400000).toISOString().split('T')[0], // Not critical
            status: "pending"
        };
        const p3 = {
            clientId: clientId,
            name: "Website Redesign",
            description: "Overhaul the main product site.",
            type: "Development",
            startDate: new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0],
            deadline: new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0], // Past due
            status: "completed"
        };

        const headers = {
            'Content-Type': 'application/json',
            'X-Manager-Id': '1'
        };

        for (const p of [p1, p2, p3]) {
            const res = await fetch("http://localhost:8081/api/projects", {
                method: "POST",
                headers,
                body: JSON.stringify(p)
            });
            console.log("Created project:", await res.json());
        }
    } catch (err) {
        console.error(err);
    }
};

createProjects();
