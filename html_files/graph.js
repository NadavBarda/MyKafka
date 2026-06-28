const canvas = document.getElementById('graphCanvas');
const ctx = canvas.getContext('2d');

let topics = [];
let agents = [];
let edges = [];

// Store positions to maintain layout across updates
const nodePositions = new Map();

// Initialize with fallback data if available
if (window.topics) {
    window.topics.forEach(t => nodePositions.set(t.id, { x: t.x, y: t.y }));
}
if (window.agents) {
    window.agents.forEach(a => nodePositions.set(a.id, { x: a.x, y: a.y }));
}

function getOrCreatePosition(id) {
    if (!nodePositions.has(id)) {
        // Assign a random position within canvas bounds (with padding)
        nodePositions.set(id, {
            x: 50 + Math.random() * 500,
            y: 50 + Math.random() * 300
        });
    }
    return nodePositions.get(id);
}

function drawArrow(fromX, fromY, toX, toY) {
    const headlen = 10; // length of head in pixels
    const dx = toX - fromX;
    const dy = toY - fromY;
    const angle = Math.atan2(dy, dx);
    
    // Draw line
    ctx.beginPath();
    ctx.moveTo(fromX, fromY);
    ctx.lineTo(toX, toY);
    ctx.strokeStyle = '#2c3e50';
    ctx.lineWidth = 1.5;
    ctx.stroke();
    
    // Draw arrowhead
    ctx.beginPath();
    ctx.moveTo(toX, toY);
    ctx.lineTo(toX - headlen * Math.cos(angle - Math.PI / 6), toY - headlen * Math.sin(angle - Math.PI / 6));
    ctx.lineTo(toX - headlen * Math.cos(angle + Math.PI / 6), toY - headlen * Math.sin(angle + Math.PI / 6));
    ctx.lineTo(toX, toY);
    ctx.fillStyle = '#2c3e50';
    ctx.fill();
}

function draw() {
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw edges
    edges.forEach(edge => {
        const fromPos = getOrCreatePosition(edge.from);
        const toPos = getOrCreatePosition(edge.to);
        
        // Ensure both positions exist
        if (fromPos && toPos) {
            drawArrow(fromPos.x, fromPos.y, toPos.x, toPos.y);
        }
    });

    // Draw Topics (Rectangles)
    topics.forEach(topic => {
        const pos = getOrCreatePosition(topic.id);
        const width = 40;
        const height = 40;
        
        ctx.fillStyle = '#a8e6cf'; // light green
        ctx.fillRect(pos.x - width/2, pos.y - height/2, width, height);
        ctx.strokeStyle = '#3b7a57';
        ctx.lineWidth = 2;
        ctx.strokeRect(pos.x - width/2, pos.y - height/2, width, height);
        
        ctx.fillStyle = '#333';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(topic.id, pos.x, pos.y + 4);
        
        // Draw value above
        ctx.font = 'bold 12px Arial';
        ctx.fillText(topic.value, pos.x, pos.y - height/2 - 5);
    });

    // Draw Agents (Circles)
    agents.forEach(agent => {
        const pos = getOrCreatePosition(agent.id);
        const radius = 25;
        
        ctx.beginPath();
        ctx.arc(pos.x, pos.y, radius, 0, 2 * Math.PI);
        ctx.fillStyle = '#bae1ff'; // light blue
        ctx.fill();
        ctx.strokeStyle = '#4682b4';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        ctx.fillStyle = '#333';
        ctx.font = '10px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(agent.id, pos.x, pos.y + 3);
    });
}

function fetchGraphData() {
    fetch('/api/graph')
        .then(response => response.json())
        .then(data => {
            topics = data.topics || [];
            agents = data.agents || [];
            edges = data.edges || [];
            draw();
        })
        .catch(error => {
            console.error('Error fetching graph data:', error);
            // If fetch fails, we could optionally draw fallback data here
            if (topics.length === 0 && window.topics) {
                topics = window.topics;
                agents = window.agents || [];
                edges = window.edges || [];
                draw();
            }
        });
}

// Initial draw using fallback data if any
if (window.topics) {
    topics = window.topics;
    agents = window.agents || [];
    edges = window.edges || [];
    draw();
}

// Start polling
fetchGraphData(); // Fetch immediately
setInterval(fetchGraphData, 1000); // Poll every second
