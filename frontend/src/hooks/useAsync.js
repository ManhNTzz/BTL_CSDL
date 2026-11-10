import { useCallback, useEffect, useRef, useState } from 'react';

export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const latest = useRef(0);

  const run = useCallback(async () => {
    const id = ++latest.current;
    setState({ data: null, loading: true, error: null });
    try {
      const data = await fn();
      if (id === latest.current) setState({ data, loading: false, error: null });
    } catch (error) {
      if (id === latest.current) setState({ data: null, loading: false, error });
    }

  }, deps);

  useEffect(() => {
    run();
    return () => { latest.current += 1; };
  }, [run]);

  return { ...state, reload: run };
}
