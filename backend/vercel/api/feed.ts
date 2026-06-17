import type { VercelRequest, VercelResponse } from '@vercel/node';

// This is the URL of your deployed Google Apps Script (we will get this in Phase 9)
const APPS_SCRIPT_URL = process.env.APPS_SCRIPT_URL;

export default async function handler(req: VercelRequest, res: VercelResponse) {
    if (req.method !== 'GET') {
        return res.status(405).json({ error: 'Method Not Allowed' });
    }

    if (!APPS_SCRIPT_URL) {
        return res.status(500).json({ error: 'APPS_SCRIPT_URL environment variable is missing.' });
    }

    const page = req.query.page || 0;

    try {
        const response = await fetch(`${APPS_SCRIPT_URL}?action=feed&page=${page}`);
        
        if (!response.ok) {
            throw new Error(`Apps Script responded with ${response.status}`);
        }

        const data = await response.json();

        // CACHING: This is the magic! Cache for 60 seconds at the edge.
        // This prevents draining your Google Apps Script quota if 1,000 users load the app at once.
        res.setHeader('Cache-Control', 's-maxage=60, stale-while-revalidate=30');
        res.status(200).json(data);

    } catch (error: any) {
        console.error("Feed Error:", error.message);
        res.status(500).json({ error: 'Failed to load feed' });
    }
}
