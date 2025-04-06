import { useState, useEffect } from 'react';
import { fetchImageBlobURL } from './imageFetcher';

export function useImageBlobURL(endpoint) {
  const [url, setUrl] = useState('');

  useEffect(() => {
    let isMounted = true;

    fetchImageBlobURL(endpoint).then((result) => {
      if (isMounted) setUrl(result);
    });

    return () => {
      isMounted = false;
    };
  }, [endpoint]);

  return url;
}
