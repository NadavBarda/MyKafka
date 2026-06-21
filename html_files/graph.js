const canvas = document.getElementById('graphCanvas');
const ctx = canvas.getContext('2d');

// Sample Data for (A+B)*(A-B)
const topics = [
    { id: 'A', value: '5.0', x: 100, y: 300 },
    { id: 'B', value: '3.0', x: 300, y: 100 },
    { id: 'C', value: '8.0', x: 500, y: 200 },
    { id: 'D', value: '2.0', x: 500, y: 300 },
    { id: 'E', value: '16.0', x: 500, y: 400 }
];

const agents = [
    { id: 'plus agent', x: 300, y: 200 },
    { id: 'min agent', x: 300, y: 400 },
    { id: 'mul agent', x: 400, y: 300 }
];

const edges = [
    { from: 'A', to: 'plus agent', isAgent: true },
    { from: 'B', to: 'plus agent', isAgent: true },
    { from: 'plus agent', to: 'C', isAgent: false },
    { from: 'A', to: 'min agent', isAgent: true },
    { from: 'B', to: 'min agent', isAgent: true },
    { from: 'min agent', to: 'D', isAgent: false },
    { from: 'C', to: 'mul agent', isAgent: true },
    { from: 'D', to: 'mul agent', isAgent: true },
    { from: 'mul agent', to: 'E', isAgent: false }
];

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

function getNodePosition(id, isAgentContext) {
    if (isAgentContext) {
        const agent = agents.find(a => a.id === id);
        if (agent) return { x: agent.x, y: agent.y };
    } else {
        const topic = topics.find(t => t.id === id);
        if (topic) return { x: topic.x, y: topic.y };
    }
    
    const all = [...topics, ...agents];
    const node = all.find(n => n.id === id);
    return { x: node.x, y: node.y };
}

// Draw edges
edges.forEach(edge => {
    const fromIsAgent = agents.some(a => a.id === edge.from);
    const toIsAgent = agents.some(a => a.id === edge.to);
    
    const fromPos = getNodePosition(edge.from, fromIsAgent);
    const toPos = getNodePosition(edge.to, toIsAgent);
    
    // Adjust line end to not overlap shapes perfectly (simplified)
    drawArrow(fromPos.x, fromPos.y, toPos.x, toPos.y);
});

// Draw Topics (Rectangles)
topics.forEach(topic => {
    const width = 40;
    const height = 40;
    ctx.fillStyle = '#a8e6cf'; // light green
    ctx.fillRect(topic.x - width/2, topic.y - height/2, width, height);
    ctx.strokeStyle = '#3b7a57';
    ctx.lineWidth = 2;
    ctx.strokeRect(topic.x - width/2, topic.y - height/2, width, height);
    
    ctx.fillStyle = '#333';
    ctx.font = '12px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(topic.id, topic.x, topic.y + 4);
    
    // Draw value above
    ctx.font = 'bold 12px Arial';
    ctx.fillText(topic.value, topic.x, topic.y - height/2 - 5);
});

// Draw Agents (Circles)
agents.forEach(agent => {
    const radius = 25;
    ctx.beginPath();
    ctx.arc(agent.x, agent.y, radius, 0, 2 * Math.PI);
    ctx.fillStyle = '#bae1ff'; // light blue
    ctx.fill();
    ctx.strokeStyle = '#4682b4';
    ctx.lineWidth = 2;
    ctx.stroke();
    
    ctx.fillStyle = '#333';
    ctx.font = '10px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(agent.id, agent.x, agent.y + 3);
});
