import type { VercelRequest, VercelResponse } from '@vercel/node';

const APPS_SCRIPT_URL = process.env.APPS_SCRIPT_URL;

export default async function handler(req: VercelRequest, res: VercelResponse) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method Not Allowed' });
    }

    if (!APPS_SCRIPT_URL) {
        return res.status(500).json({ error: 'APPS_SCRIPT_URL missing.' });
    }

    try {
        const body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body;

        // Validate required fields
        if (!body.postId || !body.userId) {
            return res.status(400).json({ error: 'Missing postId or userId' });
        }

        const payload = {
            action: 'delete',
            postId: body.postId,
            userId: body.userId,
        };

        const response = await fetch(APPS_SCRIPT_URL, {
            method: 'POST',
            body: JSON.stringify(payload),
            headers: { 'Content-Type': 'application/json' },
        });

        if (!response.ok) {
            throw new Error(`Apps Script responded with ${response.status}`);
        }

        const data = await response.json();
        res.status(200).json(data);

    } catch (error: any) {
        console.error("Delete Error:", error.message);
        res.status(500).json({ error: 'Failed to delete post' });
    }
}
