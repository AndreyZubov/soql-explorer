import { useParams } from 'react-router-dom';

// Placeholder — schema browser, editor, and result grid land in Step 3.
export default function ExplorerPage() {
  const { connectionId } = useParams();
  return (
    <section>
      <h1 className="text-lg font-semibold">Explorer</h1>
      <p className="text-sm text-muted-foreground">
        Connection: <code>{connectionId}</code>. Query workspace arrives in Step 3.
      </p>
    </section>
  );
}
