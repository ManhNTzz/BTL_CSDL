import { useState } from 'react';

export function useForm(initial) {
  const [values, setValues] = useState(initial);
  const bind = (name) => ({
    value: values[name] ?? '',
    onChange: (e) => setValues((v) => ({ ...v, [name]: e.target.value })),
  });
  const set = (name, value) => setValues((v) => ({ ...v, [name]: value }));
  return { values, bind, set, setValues };
}
