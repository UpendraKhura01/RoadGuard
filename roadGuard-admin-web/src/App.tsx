import React, { useEffect, useMemo, useState } from 'react';
import { Activity, AlertTriangle, ArrowDownRight, ArrowUpRight, Bell, Check, ChevronDown, ClipboardList, Clock3, Filter, LayoutDashboard, LogOut, Map as MapIcon, Menu, Moon, Plus, Search, ShieldCheck, Sun, Users, X } from 'lucide-react';
import { CircleMarker, MapContainer, Popup, TileLayer, Tooltip, useMap } from 'react-leaflet';
import { addKnownContractorIds, api, getToken, login, Report, RoadInfo, Analytics, Contractor, rememberContractorId, setToken } from './api';

type Page='Dashboard'|'Reports'|'Map'|'Reviews'|'Assignments'|'Contractors';
const defaults:Analytics={totalReports:0,pendingReports:0,verifiedReports:0,rejectedReports:0,totalUsers:0,activeContractors:0,reportsThisWeek:0,reportsThisMonth:0};
const statusTone=(s='')=>({SUBMITTED:'green',PENDING_REVIEW:'amber',VERIFIED:'teal',ASSIGNED:'blue',IN_PROGRESS:'orange',RESOLVED:'green',REJECTED:'red',AI_REJECTED:'red',DUPLICATE:'slate'} as Record<string,string>)[s]||'slate';
const label=(s?:string)=>s?.replaceAll('_',' ').toLowerCase().replace(/\b\w/g,c=>c.toUpperCase())||'—';

function RoadGuardLogo({size=25}:{size?:number}) {
 return <img src="/assets/roadguard-logo.png" width={size} height={size} alt="" className="roadguard-logo"/>;
}

export default function App(){
 const [authed,setAuthed]=useState(!!getToken()); const [darkMode,setDarkMode]=useState(()=>localStorage.getItem('rg-theme')==='dark'); const [page,setPage]=useState<Page>('Dashboard'); const [analytics,setAnalytics]=useState(defaults); const [reports,setReports]=useState<Report[]>([]); const [contractors,setContractors]=useState<Contractor[]>([]); const [query,setQuery]=useState(''); const [status,setStatus]=useState('ALL'); const [category,setCategory]=useState('ALL'); const [selected,setSelected]=useState<Report|null>(null); const [toast,setToast]=useState(''); const [busy,setBusy]=useState(false); const [mobileMenu,setMobileMenu]=useState(false);
 const notify=(s:string)=>{setToast(s);setTimeout(()=>setToast(''),3600)};
  const refresh=async()=>{setBusy(true);try{const [a,r,c]=await Promise.all([api<Analytics>('/admin/analytics'),api<Report[] | { content: Report[] }>('/admin/reports/all?page=0&size=100&sortBy=createdAt&sortDir=desc'),api<Contractor[]>('/admin/contractors')]);setAnalytics(a);setReports(Array.isArray(r)?r:r.content);setContractors(addKnownContractorIds(c));}catch(e){notify((e as Error).message)}finally{setBusy(false)}};
 useEffect(()=>{if(authed) refresh()},[authed]);
 useEffect(()=>{document.documentElement.dataset.theme=darkMode?'dark':'light';localStorage.setItem('rg-theme',darkMode?'dark':'light')},[darkMode]);
  useEffect(()=>{if(page==='Reviews'){setStatus('ALL');setCategory('ALL')}},[page]);
 const filtered=useMemo(()=>reports.filter(r=>(status==='ALL'||r.reportStatus===status)&&(category==='ALL'||(r.finalCategory||r.reportedCategory)===category)&&`${r.reportId} ${r.description||''} ${r.address||''} ${r.ward||''} ${r.city||''} ${r.reportedCategory||''} ${r.finalCategory||''}`.toLowerCase().includes(query.toLowerCase())),[reports,status,category,query]);
  const reviewFiltered=useMemo(()=>reports.filter(r=>['PENDING_REVIEW','SUBMITTED'].includes(r.reportStatus||'')&&(status==='ALL'||r.reportStatus===status)&&(category==='ALL'||(r.finalCategory||r.reportedCategory)===category)&&`${r.reportId} ${r.description||''} ${r.address||''} ${r.ward||''} ${r.city||''} ${r.reportedCategory||''} ${r.finalCategory||''}`.toLowerCase().includes(query.toLowerCase())),[reports,status,category,query]);
 if(!authed)return <><button className="login-theme-toggle" onClick={()=>setDarkMode(value=>!value)} title={darkMode?'Switch to light mode':'Switch to dark mode'}>{darkMode?<Sun size={18}/>:<Moon size={18}/>}</button><Login onSuccess={()=>setAuthed(true)}/></>;
 const nav:Page[]=['Dashboard','Reports','Map','Reviews','Assignments','Contractors'];
 return <div className="shell"><aside className={`sidebar ${mobileMenu?'open':''}`}><div className="brand"><span className="brandmark"><RoadGuardLogo size={25}/></span><span>RoadGuard<small>Municipal operations</small></span></div><div className="nav-label">WORKSPACE</div>{nav.map((n,i)=>{const Icon=[LayoutDashboard,ClipboardList,MapIcon,Clock3,Activity,Users][i];return <button key={n} className={`navitem ${page===n?'active':''}`} onClick={()=>{setPage(n);setMobileMenu(false)}}><Icon size={18}/>{n}{n==='Reviews'&&analytics.pendingReports>0&&<b className="navbadge">{analytics.pendingReports}</b>}</button>})}<div className="side-bottom"><div className="role-chip"><span className="avatar">A</span><span>Authority account<small>Authorized operations</small></span></div><button className="navitem" onClick={()=>{setToken('');setAuthed(false)}}><LogOut size={18}/>Sign out</button></div></aside>
 <main className="main"><header className="topbar"><button className="iconbtn mobile-only" onClick={()=>setMobileMenu(!mobileMenu)}><Menu/></button><div className="global-search"><Search size={17}/><input placeholder="Search reports, locations..." value={query} onChange={e=>setQuery(e.target.value)} onFocus={()=>setPage('Reports')}/><kbd>⌘ K</kbd></div><div className="top-actions"><button className="iconbtn" onClick={()=>setDarkMode(value=>!value)} title={darkMode?'Switch to light mode':'Switch to dark mode'}>{darkMode?<Sun size={18}/>:<Moon size={18}/>}</button><button className="iconbtn" onClick={()=>refresh()} title="Refresh"><Activity size={18}/></button><button className="iconbtn" onClick={()=>setPage('Reviews')} title="Review queue"><Bell size={18}/></button><span className="top-divider"/><span className="avatar">A</span><span className="top-user">Authority<small>RoadGuard admin</small></span><ChevronDown size={15}/></div></header>
 <section className="content">{page==='Dashboard'&&<Dashboard analytics={analytics} reports={reports} open={setSelected} setPage={setPage}/>}{page==='Reports'&&<ReportsPage reports={filtered} all={reports} status={status} setStatus={setStatus} category={category} setCategory={setCategory} query={query} setQuery={setQuery} open={setSelected} busy={busy}/>}{page==='Map'&&<MapPage reports={reports} open={setSelected}/>}{page==='Reviews'&&<ReportsPage reports={reviewFiltered} all={reports} status={status} setStatus={setStatus} category={category} setCategory={setCategory} query={query} setQuery={setQuery} open={setSelected} busy={busy} reviewOnly/>}{page==='Assignments'&&<Assignments reports={reports} contractors={contractors} refresh={refresh} notify={notify}/>}{page==='Contractors'&&<ContractorsPage contractors={contractors} refresh={refresh} notify={notify}/>}</section></main>
 {selected&&<ReportModal report={selected} close={()=>setSelected(null)} notify={notify} refresh={refresh}/>}{toast&&<div className="toast"><Check size={17}/>{toast}<button onClick={()=>setToast('')}><X size={15}/></button></div>}</div>
}

function IndianPhoneInput({value,onChange}:{value:string;onChange:(value:string)=>void}){return <div className="phone-input"><span className="phone-prefix">+91</span><input type="tel" inputMode="numeric" required maxLength={10} pattern="[6-9][0-9]{9}" value={value} onChange={e=>onChange(e.target.value.replace(/\D/g,'').slice(0,10))} placeholder="10-digit mobile number"/></div>}
function Login({onSuccess}:{onSuccess:()=>void}){const [phone,setPhone]=useState('');const [password,setPassword]=useState('');const [busy,setBusy]=useState(false);const [error,setError]=useState('');return <div className="login-screen"><div className="login-card"><div className="brand login-brand"><span className="brandmark"><RoadGuardLogo size={25}/></span><span>RoadGuard<small>Municipal operations</small></span></div><h1>Welcome back</h1><p>Sign in with an authorized administrator account.</p><form onSubmit={async e=>{e.preventDefault();setBusy(true);setError('');try{await login(phone,password);onSuccess()}catch(err){setError((err as Error).message)}finally{setBusy(false)}}}><label>Phone number<IndianPhoneInput value={phone} onChange={setPhone}/></label><label>Password<input required type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Enter password"/></label>{error&&<div className="errorbox">{error}</div>}<button className="primary full" disabled={busy}>{busy?'Signing in…':'Sign in securely'}</button></form><div className="login-note"><ShieldCheck size={16}/> Role checked by the backend · reporter identity is not shown in this console</div></div></div>}

function PageHeading({eyebrow,title,description,action}:{eyebrow:string;title:string;description:string;action?:React.ReactNode}){return <div className="page-heading"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{description}</p></div>{action}</div>}
function Dashboard({analytics:a,reports,open,setPage}:{analytics:Analytics;reports:Report[];open:(r:Report)=>void;setPage:(p:Page)=>void}){const cards: [string, number, string, React.ComponentType<{size?: number}>, string][]=[['Total reports',a.totalReports,'Across service area',ClipboardList,'blue'],['Needs review',a.pendingReports,'Awaiting a decision',Clock3,'amber'],['Resolved',a.verifiedReports,'Verified reports',Check,'green'],['Rejected',a.rejectedReports,'Review outcomes',AlertTriangle,'red'],['Active contractors',a.activeContractors,'Available in directory',Users,'purple']];return <><PageHeading eyebrow="OVERVIEW" title="Operations dashboard" description="Review road hazards and coordinate repairs across your service area." action={<button className="secondary" onClick={()=>setPage('Reports')}><Filter size={16}/> Browse reports</button>}/><div className="stat-grid">{cards.map(([name,value,detail,Icon,tone])=>{const CardIcon=Icon as React.ComponentType<{size:number}>;return <article className="stat-card" key={String(name)}><div className={`stat-icon ${tone}`}><CardIcon size={20}/></div><div className="stat-label">{name}</div><div className="stat-value">{value}</div><div className="stat-foot">{detail}</div></article>})}</div><div className="dash-grid"><section className="panel map-panel"><div className="panel-head"><div><h2>Hazard map</h2><p>Reported locations, with no submitter identity</p></div><button className="text-button" onClick={()=>setPage('Map')}>Full map <ArrowUpRight size={15}/></button></div><HazardMap reports={reports.slice(0,80)} height="340px" open={open}/></section><section className="panel urgent-panel"><div className="panel-head"><div><h2>Recent reports</h2><p>Latest submissions</p></div><button className="text-button" onClick={()=>setPage('Reports')}>View all <ArrowUpRight size={15}/></button></div><div className="compact-list">{reports.slice(0,5).map(r=><button className="compact-row" key={r.reportId} onClick={()=>open(r)}><span className={`dot ${statusTone(r.reportStatus)}`}/><span className="compact-info"><b>{label(r.finalCategory||r.reportedCategory)}</b><small>{r.road||r.fullAddress||r.address||r.road||r.fullAddress||r.address||r.ward||r.city||'Location recorded'} · #{r.reportId}</small></span><span className={`pill ${statusTone(r.reportStatus)}`}>{label(r.reportStatus)}</span></button>)}</div></section></div><section className="panel table-panel"><div className="panel-head"><div><h2>Priority queue</h2><p>Submitted and pending review reports</p></div><button className="text-button" onClick={()=>setPage('Reviews')}>Open queue <ArrowUpRight size={15}/></button></div><ReportTable reports={reports.filter(r=>['PENDING_REVIEW','SUBMITTED'].includes(r.reportStatus||'')).slice(0,6)} open={open}/></section></>}

function ReportsPage({reports,all,status,setStatus,category,setCategory,query,setQuery,open,busy,reviewOnly=false}:{reports:Report[];all:Report[];status:string;setStatus:(s:string)=>void;category:string;setCategory:(s:string)=>void;query:string;setQuery:(s:string)=>void;open:(r:Report)=>void;busy:boolean;reviewOnly?:boolean}){const categories=Array.from(new Set(all.map(r=>r.finalCategory||r.reportedCategory).filter(Boolean))) as string[];const statuses=reviewOnly?['SUBMITTED','PENDING_REVIEW']:['SUBMITTED','PENDING_REVIEW','VERIFIED','ASSIGNED','IN_PROGRESS','RESOLVED','REJECTED','AI_REJECTED','DUPLICATE'];return <><PageHeading eyebrow={reviewOnly?'REVIEW WORKSPACE':'REPORT MANAGEMENT'} title={reviewOnly?'Needs review':'All reports'} description={reviewOnly?'Assess uncertain submissions and record an auditable decision.':'Search and filter reports by status, category, and location.'}/><div className="panel filterbar"><div className="filter-search"><Search size={17}/><input placeholder="Search ID, description, location" value={query} onChange={e=>setQuery(e.target.value)}/></div><select value={status} onChange={e=>setStatus(e.target.value)}><option value="ALL">All statuses</option>{statuses.map(s=><option key={s} value={s}>{label(s)}</option>)}</select><select value={category} onChange={e=>setCategory(e.target.value)}><option value="ALL">All categories</option>{categories.map(c=><option key={c}>{c}</option>)}</select><button className="secondary" onClick={()=>{setStatus('ALL');setCategory('ALL');setQuery('')}}><X size={15}/> Clear</button><span className="result-count">{busy?'Loading…':`${reports.length} reports`}</span></div><section className="panel table-panel"><ReportTable reports={reports} open={open} empty={reviewOnly?'No reports awaiting review. Completed reports appear under All Reports.':'No reports match these filters.'}/></section></>}

function ReportTable({reports,open,empty='No reports available.'}:{reports:Report[];open:(r:Report)=>void;empty?:string}){return <div className="table-wrap"><table><thead><tr><th>Report</th><th>Location</th><th>Severity</th><th>Status</th><th>Submitted</th><th/></tr></thead><tbody>{reports.length===0?<tr><td colSpan={6} className="empty">{empty}</td></tr>:reports.map(r=><tr key={r.reportId} onClick={()=>open(r)}><td><b>#{r.reportId} · {label(r.finalCategory||r.reportedCategory)}</b><small>{r.description||'No description provided'}</small></td><td>{r.road||r.fullAddress||r.address||r.ward||r.city||'—'}</td><td><span className={`pill ${r.finalSeverity==='HIGH'||r.aiSeverity==='HIGH'?'red':r.finalSeverity==='MEDIUM'||r.aiSeverity==='MEDIUM'?'amber':'neutral'}`}>{label(r.finalSeverity||r.aiSeverity)}</span></td><td><span className={`pill ${statusTone(r.reportStatus)}`}>{label(r.reportStatus)}</span></td><td>{r.createdAt?new Date(r.createdAt).toLocaleDateString():'—'}</td><td><button className="row-open" onClick={e=>{e.stopPropagation();open(r)}}>Details <ArrowUpRight size={14}/></button></td></tr>)}</tbody></table></div>}

function CenterOnBrowserLocation({onLocate}:{onLocate:(point:[number,number])=>void}){const map=useMap();useEffect(()=>{if(!navigator.geolocation)return;navigator.geolocation.getCurrentPosition(({coords})=>{const point:[number,number]=[coords.latitude,coords.longitude];onLocate(point);map.flyTo(point,14,{animate:false})},()=>{},{enableHighAccuracy:true,timeout:12000,maximumAge:60000})},[map,onLocate]);return null}
type ReportCluster = { reports: Report[]; center: [number, number] };
function clusterReports(reports: Report[], zoom: number): ReportCluster[] {
  const thresholdMeters = 100 * Math.pow(2, 15 - zoom);
  const groups: ReportCluster[] = [];
  reports.forEach(report => {
    const point: [number, number] = [report.latitude!, report.longitude!];
    const group = groups.find(candidate => {
      const meanLat = (point[0] + candidate.center[0]) / 2;
      const dy = (point[0] - candidate.center[0]) * 111320;
      const dx = (point[1] - candidate.center[1]) * 111320 * Math.cos(meanLat * Math.PI / 180);
      return Math.sqrt(dx * dx + dy * dy) <= thresholdMeters;
    });
    if (!group) groups.push({ reports: [report], center: point });
    else {
      group.reports.push(report);
      const count = group.reports.length;
      group.center = [
        group.center[0] + (point[0] - group.center[0]) / count,
        group.center[1] + (point[1] - group.center[1]) / count,
      ];
    }
  });
  return groups;
}
const pinColor = (status?: string) => ({
  SUBMITTED: '#278442', PENDING_REVIEW: '#b67a00', VERIFIED: '#087c69',
  ASSIGNED: '#1766a8', IN_PROGRESS: '#b85c00', RESOLVED: '#1f6333',
  REJECTED: '#b63831', AI_REJECTED: '#b63831', DUPLICATE: '#697783',
} as Record<string, string>)[status || ''] || '#697783';

function ClusteredReports({ reports, open }: { reports: Report[]; open: (r: Report) => void }) {
  const map = useMap();
  const [zoom, setZoom] = useState(map.getZoom());
  useEffect(() => {
    const updateZoom = () => setZoom(map.getZoom());
    map.on('zoomend', updateZoom);
    return () => { map.off('zoomend', updateZoom); };
  }, [map]);
  const groups = useMemo(() => clusterReports(reports, zoom), [reports, zoom]);
  return <>{groups.map(group => {
    const clustered = group.reports.length > 1;
    const key = group.reports.map(report => report.reportId).join('-');
    return <CircleMarker key={key} center={group.center} radius={clustered ? 17 : 8}
      pathOptions={{ color: '#fff', weight: 2, fillColor: clustered ? '#173247' : pinColor(group.reports[0].reportStatus), fillOpacity: .96 }}
      eventHandlers={{ click: () => { if (!clustered) open(group.reports[0]); } }}>
      {clustered && <Tooltip permanent direction="center" className="cluster-count">{group.reports.length}</Tooltip>}
      <Popup minWidth={235}>
        <div className="map-cluster-list">
          <b>{clustered ? group.reports.length + ' reports at this location' : 'Report #' + group.reports[0].reportId}</b>
          {group.reports.map(report => <button key={report.reportId} onClick={() => open(report)}>
            <span className="cluster-status-dot" style={{ background: pinColor(report.reportStatus) }} />
            <span><strong>#{report.reportId} · {label(report.finalCategory || report.reportedCategory)}</strong>
              <small>{label(report.reportStatus)} · {report.road || report.fullAddress || report.address || report.ward || report.city || 'Location recorded'}</small></span>
          </button>)}
          {clustered && <small className="cluster-hint">Zoom in to see these reports as separate pins.</small>}
        </div>
      </Popup>
    </CircleMarker>;
  })}</>;
}

function HazardMap({reports,height,open}:{reports:Report[];height:string;open:(r:Report)=>void}) {
  const valid = reports.filter(r => typeof r.latitude === 'number' && typeof r.longitude === 'number');
  const [userLocation,setUserLocation] = useState<[number,number]|null>(null);
  const initialCenter:[number,number] = valid.length ? [valid[0].latitude!,valid[0].longitude!] : [20.2961,85.8245];
  return <MapContainer center={userLocation||initialCenter} zoom={userLocation?14:12} scrollWheelZoom style={{height,width:'100%',borderRadius:12}}>
    <CenterOnBrowserLocation onLocate={setUserLocation}/>
    <TileLayer attribution="&copy; OpenStreetMap contributors" url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"/>
    {userLocation&&<CircleMarker center={userLocation} radius={9} pathOptions={{color:'#fff',weight:3,fillColor:'#1976d2',fillOpacity:1}}><Popup>Your current location</Popup></CircleMarker>}
    <ClusteredReports reports={valid} open={open}/>
  </MapContainer>;
}function MapPage({reports,open}:{reports:Report[];open:(r:Report)=>void}){return <><PageHeading eyebrow="GEOGRAPHIC VIEW" title="Hazard map" description="Explore report locations. Reporter identities are excluded."/><section className="panel map-full"><HazardMap reports={reports} height="min(68vh,720px)" open={open}/><div className="map-legend"><span><i className="dot amber"/>Active / needs review</span><span><i className="dot green"/>Resolved</span><span>{reports.length} reports loaded</span></div></section></>}

function ReportModal({report:r,close,notify,refresh}:{report:Report;close:()=>void;notify:(s:string)=>void;refresh:()=>void}) {
  const [decision,setDecision]=useState('APPROVE');
  const [cat,setCat]=useState(r.finalCategory||r.reportedCategory||'POTHOLE');
  const [severity,setSeverity]=useState(r.finalSeverity||r.aiSeverity||'MEDIUM');
  const [comment,setComment]=useState('');
  const [newStatus,setNewStatus]=useState('IN_PROGRESS');
  const [notes,setNotes]=useState('');
  const [busy,setBusy]=useState(false);
  const [roadInfo,setRoadInfo]=useState<RoadInfo|null>(null);
  const [roadInfoLoading,setRoadInfoLoading]=useState(true);
  const [roadInfoError,setRoadInfoError]=useState(false);
  const canReview=['PENDING_REVIEW','SUBMITTED'].includes(r.reportStatus||'');
  const categories=['POTHOLE','OPEN_MANHOLE','WATERLOGGING','FALLEN_TREE','BROKEN_STREETLIGHT','DAMAGED_ROAD','BROKEN_TRAFFIC_SIGNAL','OTHER'];

  useEffect(()=>{
    let cancelled=false;
    setRoadInfo(null);
    setRoadInfoError(false);
    setRoadInfoLoading(true);
    if(r.latitude==null||r.longitude==null){
      setRoadInfoError(true);
      setRoadInfoLoading(false);
      return ()=>{cancelled=true};
    }
    api<RoadInfo>(`/roadInfo/getRoadInfo/${r.latitude}/${r.longitude}`)
      .then(value=>{if(!cancelled)setRoadInfo(value)})
      .catch(()=>{if(!cancelled)setRoadInfoError(true)})
      .finally(()=>{if(!cancelled)setRoadInfoLoading(false)});
    return ()=>{cancelled=true};
  },[r.reportId]);

  const finish=async(path:string,body:unknown,msg:string)=>{
    setBusy(true);
    try{await api(path,{method:'POST',body:JSON.stringify(body)});notify(msg);await refresh();close()}
    catch(e){notify((e as Error).message)}
    finally{setBusy(false)}
  };
  const formatDate=(value?:string)=>value?new Date(value).toLocaleDateString():'—';

  return <div className="modal-scrim" onMouseDown={e=>{if(e.target===e.currentTarget)close()}}>
    <div className="modal">
      <div className="modal-head"><div><div className="eyebrow">REPORT #{r.reportId}</div><h2>{label(r.finalCategory||r.reportedCategory)}</h2></div><button className="iconbtn" onClick={close}><X/></button></div>
      <div className="modal-body">
        {r.imageUrl&&<img className="report-photo" src={r.imageUrl} alt="Submitted road hazard"/>}
        <div className="detail-grid">
          <Detail label="Status" value={label(r.reportStatus)}/>
          <Detail label="AI severity" value={label(r.aiSeverity)}/>
          <Detail label="Final severity" value={label(r.finalSeverity)}/>
          <Detail label="Location" value={[r.road,r.fullAddress,r.address,r.ward,r.city,r.district,r.state].filter(Boolean).join(', ')||'—'}/>
          <Detail label="Coordinates" value={r.latitude!=null&&r.longitude!=null?`${r.latitude.toFixed(5)}, ${r.longitude.toFixed(5)}`:'—'}/>
          <Detail label="Submitted" value={r.createdAt?new Date(r.createdAt).toLocaleString():'—'}/>
          <Detail label="Assigned report contractor" value={r.contractorName?`${r.contractorName}${r.contractorId?` · ID ${r.contractorId}`:''}`:'Not assigned'}/>
        </div>
        <div className="description-box">
          <b>Nearby road record</b>
          {roadInfoLoading&&<p>Loading road information…</p>}
          {!roadInfoLoading&&roadInfoError&&<p>No nearby road record was found, or its details could not be loaded.</p>}
          {!roadInfoLoading&&roadInfo&&<div className="detail-grid">
            <Detail label="Road" value={roadInfo.name||'—'}/>
            <Detail label="Road-record contractor" value={roadInfo.contractorName||'—'}/>
            <Detail label="Contractor phone" value={roadInfo.contractorContact||'—'}/>
            <Detail label="Contractor email" value={roadInfo.contractorEmail||'—'}/>
            <Detail label="Budget" value={roadInfo.budget==null?'—':String(roadInfo.budget)}/>
            <Detail label="Last maintenance" value={formatDate(roadInfo.lastMaintenance)}/>
            <Detail label="Warranty until" value={formatDate(roadInfo.warrantyTill)}/>
          </div>}
        </div>
        <div className="description-box"><b>Description</b><p>{r.description||'No description provided.'}</p></div>
        {r.aiExplanation&&<div className="ai-box"><b><ShieldCheck size={15}/> AI assessment signal</b><p>{r.aiExplanation}</p></div>}
        {canReview?<div className="form-section"><h3>Record review decision</h3><div className="form-row"><label>Decision<select value={decision} onChange={e=>setDecision(e.target.value)}>{['APPROVE','REJECT','NEED_MORE_INFO','MARK_DUPLICATE'].map(v=><option key={v}>{v}</option>)}</select></label><label>Final category<select value={cat} onChange={e=>setCat(e.target.value)}>{categories.map(v=><option key={v}>{v}</option>)}</select></label><label>Final severity<select value={severity} onChange={e=>setSeverity(e.target.value)}>{['LOW','MEDIUM','HIGH','CRITICAL'].map(v=><option key={v}>{v}</option>)}</select></label></div><label>Admin note<textarea value={comment} onChange={e=>setComment(e.target.value)} placeholder="Reason for this decision"/></label><button className="primary" disabled={busy} onClick={()=>finish(`/admin/reviewReport/${r.reportId}`,{finalDecision:decision,finalCategory:cat,finalSeverity:severity,comment},'Review decision saved')}>Save decision</button></div>:<div className="notice-box">This report has already left the review queue. Find it under All Reports; use the status section below for repair progress.</div>}
        <div className="form-section"><h3>Update repair status</h3><div className="form-row"><label>New status<select value={newStatus} onChange={e=>setNewStatus(e.target.value)}>{['VERIFIED','ASSIGNED','IN_PROGRESS','RESOLVED','REJECTED','DUPLICATE'].map(v=><option key={v}>{v}</option>)}</select></label><label className="wide">Update note<input value={notes} onChange={e=>setNotes(e.target.value)} placeholder="Status update note"/></label></div><button className="secondary" disabled={busy} onClick={()=>finish(`/admin/updateReportStatus/${r.reportId}`,{newStatus,notes},'Report status updated')}>Update status</button></div>
      </div>
    </div>
  </div>;
}

function Detail({label,value}:{label:string;value:string}){return <div className="detail"><small>{label}</small><b>{value||'—'}</b></div>}

function Assignments({reports,contractors,refresh,notify}:{reports:Report[];contractors:Contractor[];refresh:()=>void;notify:(s:string)=>void}){const [rid,setRid]=useState('');const [cid,setCid]=useState('');const [manualId,setManualId]=useState('');const [busy,setBusy]=useState(false);const assignId=cid||manualId;const doAssign=async()=>{setBusy(true);try{await api('/admin/contractor/assign',{method:'POST',body:JSON.stringify({reportId:Number(rid),contractorId:Number(assignId)})});notify('Contractor assigned');await refresh()}catch(e){notify((e as Error).message)}finally{setBusy(false)}};return <><PageHeading eyebrow="FIELD OPERATIONS" title="Assignments" description="Choose a report and contractor to create an assignment."/><div className="assign-grid"><section className="panel form-panel"><h2>Assign contractor</h2><p>Select a contractor whose ID is available. Newly created contractors are remembered in this browser.</p><label>Report<select value={rid} onChange={e=>setRid(e.target.value)}><option value="">Choose a report</option>{reports.filter(r=>!['RESOLVED','REJECTED','AI_REJECTED'].includes(r.reportStatus||'')).map(r=><option key={r.reportId} value={r.reportId}>#{r.reportId} · {label(r.finalCategory||r.reportedCategory)} · {label(r.reportStatus)}</option>)}</select></label><label>Contractor<select value={cid} onChange={e=>{setCid(e.target.value);setManualId('')}}><option value="">Choose a contractor</option>{contractors.map((c,i)=><option key={`${c.phoneNumber||c.name}-${i}`} value={c.id??''} disabled={c.id==null}>{c.name}{c.id!=null?` · ID ${c.id}`:' · ID unavailable'}</option>)}</select></label>{contractors.some(c=>c.id==null)&&<div className="notice-box">The backend omits IDs for existing contractors. Add a new contractor to capture its ID, or enter a known ID below. IDs cannot be safely inferred from names.</div>}<details className="manual-id"><summary>Enter a known contractor ID</summary><input inputMode="numeric" value={manualId} onChange={e=>{setManualId(e.target.value);setCid('')}} placeholder="Contractor ID"/></details><button className="primary" onClick={doAssign} disabled={busy||!rid||!assignId}>Assign contractor</button></section><section className="panel"><div className="panel-head"><div><h2>Contractor directory</h2><p>{contractors.length} contractors returned by the backend</p></div></div><ContractorTable contractors={contractors}/></section></div></>}
function ContractorsPage({contractors,refresh,notify}:{contractors:Contractor[];refresh:()=>void;notify:(s:string)=>void}){const [open,setOpen]=useState(false);const [form,setForm]=useState({name:'',phoneNumber:'',area:'',specialization:''});const [busy,setBusy]=useState(false);const save=async(e:React.FormEvent)=>{e.preventDefault();setBusy(true);try{const created=await api<Contractor>('/admin/contractor/create',{method:'POST',body:JSON.stringify(form)});rememberContractorId(created);notify(created.id!=null?`Contractor created · ID ${created.id}`:'Contractor created');setOpen(false);setForm({name:'',phoneNumber:'',area:'',specialization:''});await refresh()}catch(err){notify((err as Error).message)}finally{setBusy(false)}};return <><PageHeading eyebrow="FIELD OPERATIONS" title="Contractors & officers" description="Manage contractor records available for report assignment." action={<button className="primary" onClick={()=>setOpen(true)}><Plus size={17}/> Add contractor</button>}/><section className="panel table-panel"><ContractorTable contractors={contractors}/></section>{open&&<div className="modal-scrim"><form className="modal contractor-modal" onSubmit={save}><div className="modal-head"><div><div className="eyebrow">DIRECTORY</div><h2>Add contractor</h2></div><button type="button" className="iconbtn" onClick={()=>setOpen(false)}><X/></button></div><div className="modal-body"><label>Name<input required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Phone number<IndianPhoneInput value={form.phoneNumber} onChange={value=>setForm({...form,phoneNumber:value})}/></label><label>Service area<input required value={form.area} onChange={e=>setForm({...form,area:e.target.value})}/></label><label>Specialization<input value={form.specialization} onChange={e=>setForm({...form,specialization:e.target.value})}/></label><button className="primary" disabled={busy}>{busy?'Saving…':'Create contractor'}</button></div></form></div>}</>}
function ContractorTable({contractors}:{contractors:Contractor[]}){return <div className="table-wrap"><table><thead><tr><th>Name</th><th>Contractor ID</th><th>Service area</th><th>Specialization</th><th>Phone</th></tr></thead><tbody>{contractors.length===0?<tr><td colSpan={5} className="empty">No contractors found.</td></tr>:contractors.map((c,i)=><tr key={`${c.name}-${i}`}><td><b>{c.name}</b></td><td>{c.id??'Unavailable from backend'}</td><td>{c.area||'—'}</td><td>{c.specialization||'—'}</td><td>{c.phoneNumber||'—'}</td></tr>)}</tbody></table></div>}


