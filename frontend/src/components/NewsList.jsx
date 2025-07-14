import { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';

function formatExactTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: true
  });
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function NewsList() {
  const { sourceId } = useParams();
  const location = useLocation();
  const { name, logo } = location.state || {};
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!API_BASE_URL) return;
    fetch(`${API_BASE_URL}/api/articles?sourceId=${sourceId}`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch articles');
        return res.json();
      })
      .then((data) => {
        setArticles(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [sourceId]);

  if (!API_BASE_URL) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">API base URL is not set. Please set VITE_API_BASE_URL in your .env file.</span></div>;
  if (loading) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-gray-700">Loading articles...</span></div>;
  if (error) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">{error}</span></div>;

  return (
    <div className="flex flex-col items-center pt-32 pb-12 px-4">
      <div className="flex items-center gap-4 mb-8">
        {logo && <img src={logo} alt={name + ' logo'} className="h-12 w-auto object-contain" />}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl">
        {articles.length === 0 ? (
          <div className="col-span-full text-gray-500 text-center text-lg">No articles found.</div>
        ) : (
          articles.map((article, idx) => (
            <div key={idx} className="bg-white/90 rounded-lg shadow p-6 flex flex-col gap-3 border border-gray-200 hover:shadow-lg transition-shadow">
              {article.imageUrl && (
                <img src={article.imageUrl} alt={article.title} className="w-full h-48 object-cover rounded mb-2" />
              )}
              <div className="text-xl font-semibold text-gray-800 mb-1">{article.title}</div>
              {article.publishedAt && (
                <div
                  className="inline-block px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold mb-1 w-fit self-start"
                >
                  {formatExactTime(article.publishedAt)}
                </div>
              )}
              {article.description && <div className="text-gray-600 text-base mb-2">{article.description}</div>}
              <a href={article.link} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline text-sm font-medium mt-auto">Read Full Article</a>
            </div>
          ))
        )}
      </div>
    </div>
  );
} 