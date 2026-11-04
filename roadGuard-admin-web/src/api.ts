const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/roadguard/v1';
export type Report = {
  reportId: number;
  description?: string;
  reportedCategory?: string;
  finalCategory?: string;
  aiSeverity?: string;
  finalSeverity?: string;
  reportStatus?: string;
  latitude?: number;
  longitude?: number;
  createdAt?: string;
  updatedAt?: string;
  address?: string;
  fullAddress?: string;
  city?: string;
  ward?: string;
  road?: string;
  block?: string;
  district?: string;
  state?: string;
  imageUrl?: string;
  aiExplanation?: string;
  decisionReason?: string;
  contractorId?: number;
  contractorName?: string;
};
export type RoadInfo = {
  name?: string;
  contractorName?: string;
  contractorContact?: string;
  contractorEmail?: string;
  lastMaintenance?: string;
  warrantyTill?: string;
  budget?: number;
};
export type Analytics = { totalReports:number; pendingReports:number; verifiedReports:number; rejectedReports:number; totalUsers:number; activeContractors:number; reportsThisWeek:number; reportsThisMonth:number };
export type Contractor = { id?:number; name:string; phoneNumber?:string; email?:string; area?:string; specialization?:string };
const contractorIdCacheKey = 'rg-known-contractor-ids';
export function rememberContractorId(contractor: Contractor) {
  if (contractor.id == null || !contractor.phoneNumber) return;
  const known = JSON.parse(localStorage.getItem(contractorIdCacheKey) || '{}') as Record<string, number>;
  known[contractor.phoneNumber] = contractor.id;
  localStorage.setItem(contractorIdCacheKey, JSON.stringify(known));
}
export function addKnownContractorIds(contractors: Contractor[]) {
  const known = JSON.parse(localStorage.getItem(contractorIdCacheKey) || '{}') as Record<string, number>;
  return contractors.map(contractor => ({
    ...contractor,
    id: contractor.id ?? (contractor.phoneNumber ? known[contractor.phoneNumber] : undefined),
  }));
}
let token = localStorage.getItem('rg-token') || '';
export function setToken(value:string) { token=value; if(value) localStorage.setItem('rg-token',value); else localStorage.removeItem('rg-token'); }
export function getToken() { return token; }
export async function api<T>(path:string, init:RequestInit={}) : Promise<T> {
  const headers = new Headers(init.headers); if(token) headers.set('Authorization',`Bearer ${token}`);
  if(init.body && !(init.body instanceof FormData)) headers.set('Content-Type','application/json');
  const response=await fetch(`${base}${path}`,{...init,headers});
  const text=await response.text(); let data:any=null; try{data=text?JSON.parse(text):null}catch{data=text;}
  if(!response.ok) throw new Error(data?.message||data?.error||`Request failed (${response.status})`);
  return data as T;
}
export async function login(phoneNumber:string,password:string){
  const result=await api<{token:string;role:string;name?:string}>('/auth/login/password',{method:'POST',body:JSON.stringify({phoneNumber,password})});
  if(!['ADMIN','AUTHORITY'].includes(result.role)) throw new Error('This account does not have admin access.');
  setToken(result.token); return result;
}

