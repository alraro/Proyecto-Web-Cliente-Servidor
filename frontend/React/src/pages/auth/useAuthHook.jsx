import { useContext } from 'react';
import { ContextoAuten } from './ContextoAuten';

export const useAuth = () => useContext(ContextoAuten);