export default function ErrorCard({ message, onRetry }) {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="bg-red-900/20 border border-red-500/30 text-red-400 px-6 py-4 rounded-lg max-w-md text-center">
        <h2 className="text-lg font-semibold mb-2">
          Something went wrong
        </h2>
        <p className="text-sm mb-4">
          {message || "Please try again later."}
        </p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="mt-2 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-500/50 rounded-lg text-sm transition-colors"
          >
            Try Again
          </button>
        )}
      </div>
    </div>
  );
}
