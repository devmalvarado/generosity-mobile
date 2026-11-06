import {centers, categories, needs, screens, filterCenters, routeInfo, toggleSaved} from './model.mjs?v=7e49a455fa16';
import {pageContent} from './pages.mjs?v=1f86924caaa1';

const state={route:'map',language:'es',dark:false,category:'all',funding:'all',query:'',selected:'c1',saved:['c1'],origin:false,mode:'all',scenario:'normal',stack:[],joined:false,meal:'',organization:false,individualRank:false,read:false,offer:false};
try { const locale=localStorage.getItem('generosity.locale'); state.language=['es','en'].includes(locale)?locale:((navigator.languages||[navigator.language]).find(tag=>/^(es|en)(-|$)/i.test(tag))||'en').slice(0,2).toLowerCase(); } catch (_) { state.language=(navigator.language||'en').slice(0,2)==='es'?'es':'en'; }
const app=document.querySelector('#app'), sheet=document.querySelector('#sheet');
let tokens, toastTimer;
const t=(es,en)=>state.language==='es'?es:en;
const escape=value=>String(value).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const paths={
  map:'M3 6l6-3 6 3 6-3v15l-6 3-6-3-6 3z M9 3v15 M15 6v15',
  heart:'M20.8 4.6a5.5 5.5 0 00-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 00-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 000-7.8z',
  pin:'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1116 0z M15 10a3 3 0 11-6 0 3 3 0 016 0',
  list:'M8 6h13 M8 12h13 M8 18h13 M3 6h.1 M3 12h.1 M3 18h.1',
  filter:'M3 6h18 M3 12h18 M3 18h18 M7 3v6 M16 9v6 M10 15v6',
  search:'M17 10a7 7 0 11-14 0 7 7 0 0114 0 M15 15l6 6',
  location:'M21 3L3 10l8 3 3 8 7-18z',
  bell:'M18 8a6 6 0 00-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9 M10 21h4',
  person:'M16 7a4 4 0 11-8 0 4 4 0 018 0 M4 21v-2a8 8 0 0116 0v2',
  people:'M10 7a3 3 0 11-6 0 3 3 0 016 0 M20 7a3 3 0 11-6 0 3 3 0 016 0 M1 21v-3a6 6 0 0112 0v3 M15 13a6 6 0 018 5v3',
  gift:'M3 8h18v4H3z M5 12v9h14v-9 M12 8v13 M12 8C2 8 4 0 8 3z M12 8c10 0 8-8 4-5z',
  clock:'M22 12a10 10 0 11-20 0 10 10 0 0120 0 M12 6v6l4 2',
  home:'M2 11L12 2l10 9 M5 9v12h14V9 M9 21v-7h6v7',
  cross:'M9 3h6v6h6v6h-6v6H9v-6H3V9h6z',
  drop:'M12 2S5 10 5 15a7 7 0 0014 0c0-5-7-13-7-13z',
  meal:'M4 3v7c0 3 5 3 5 0V3 M6.5 3v18 M18 3c-4 4-4 9 0 9h2V3h-2v18',
  award:'M16 7a4 4 0 11-8 0 4 4 0 018 0 M8 11L6 22l6-4 6 4-2-11',
  star:'M12 2l3 6 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1z',
  chart:'M4 20V10 M12 20V4 M20 20v-7',
  share:'M18 4a3 3 0 110 6 3 3 0 010-6 M6 9a3 3 0 110 6 3 3 0 010-6 M18 16a3 3 0 110 6 3 3 0 010-6 M9 10l6-3 M9 14l6 5',
  calendar:'M4 5h16v16H4z M4 10h16 M8 2v6 M16 2v6 M8 14h2 M14 14h2 M8 18h2',
  shield:'M12 2l9 4v6c0 6-9 10-9 10S3 18 3 12V6z M8 12l3 3 5-6',
  layers:'M12 2L2 7l10 5 10-5z M2 12l10 5 10-5 M2 17l10 5 10-5',
  clipboard:'M8 4H4v18h16V4h-4 M8 2h8v5H8z M8 11h8 M8 16h6',
  message:'M3 3h18v14H8l-5 4z M7 8h10 M7 12h6',
  shop:'M3 3h18l1 6H2z M4 9v12h16V9 M9 21v-8h6v8',
  settings:'M12 8a4 4 0 110 8 4 4 0 010-8 M10 2h4l1 3 3 1 3 3-1 3 1 3-3 3-3 1-1 3h-4l-1-3-3-1-3-3 1-3-1-3 3-3 3-1z',
  plus:'M12 4v16 M4 12h16', back:'M15 5l-7 7 7 7', next:'M9 5l7 7-7 7', close:'M6 6l12 12 M6 18L18 6', check:'M5 12l4 4L20 5', arrow:'M5 12h14 M14 7l5 5-5 5', external:'M14 3h7v7 M10 14L21 3 M10 3H3v18h18v-7',
};
const icon=name=>`<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="${paths[name]||paths.heart}"/></svg>`;
const button=(label,action,value='',style='secondary',symbol='')=>`<button type="button" class="${style}" data-action="${action}" data-value="${escape(value)}">${symbol?icon(symbol):''}${escape(label)}</button>`;
const link=(route,label,subtitle='',symbol=routeInfo(route)[3])=>`<button class="list-link" data-route="${route}">${icon(symbol)}<span class="grow"><strong>${escape(label)}</strong>${subtitle?`<small>${escape(subtitle)}</small>`:''}</span>${icon('next')}</button>`;
const notice=text=>`<p class="notice">${escape(text)}</p>`;
const field=(label,type='text',placeholder='',required=true)=>`<label class="label">${escape(label)}<input type="${type}" ${type==='number'?'min="1"':''} placeholder="${escape(placeholder)}" ${required?'required':''}></label>`;
const choice=(label,options)=>`<label class="label">${escape(label)}<select>${options.map(o=>`<option>${escape(o)}</option>`).join('')}</select></label>`;
const name=c=>t(c.name,c.en);
const center=()=>centers.find(c=>c.id===state.selected)||centers[0];
const badge=c=>c.verified?`<span class="verified" role="img" aria-label="${t('Verificación vigente (ficticia)','Current verification (fictional)')}">${icon('check')}</span>`:'';
const funding=code=>({public:t('Público','Public'),private:t('Privado','Private'),mixed:t('Mixto','Mixed')})[code];
function centerRow(c,select=false) {
  return `<button class="list-link" data-action="${select?'select':'center'}" data-value="${c.id}"><span class="bubble ${c.category}">${icon(c.icon)}</span><span class="grow"><strong>${escape(name(c))} ${badge(c)}</strong><small>${c.distance} km · ${escape(funding(c.funding))}</small></span>${icon('next')}</button>`;
}
function needCard(n) {
  const c=centers.find(v=>v.id===n.center);
  return `<button class="card list-link" data-action="offer" data-value="${c.id}"><span class="bubble">${icon(n.icon)}</span><span class="grow"><span class="badge ${n.priority==='urgent'?'urgent':''}">${n.priority==='urgent'?t('URGENTE','URGENT'):t('VOLUNTARIADO','VOLUNTEERING')}</span><strong>${escape(t(n.title,n.en))}</strong><small>${escape(t(n.description,n.descriptionEn))}</small><small>${escape(name(c))} · ${c.distance} km</small></span>${icon('next')}</button>`;
}
const h={t,escape,icon,button,link,notice,field,choice,name,center,badge,funding,centerRow,needCard};

function mapArt() {
  return `<div class="map-art" aria-hidden="true"><svg viewBox="0 0 430 800" preserveAspectRatio="xMidYMid slice">
    <defs><pattern id="blocks" width="75" height="95" patternTransform="rotate(-18)" patternUnits="userSpaceOnUse"><rect class="block" x="5" y="5" width="65" height="85" rx="6"/></pattern></defs>
    <rect width="430" height="800" fill="url(#blocks)"/>
    <path class="park" d="M270 0h160v300l-90-25-55-80z M0 580l120-30 36 110-156 57z"/>
    <path class="avenue-edge" d="M-70 100Q180 220 160 410T480 720"/><path class="avenue" d="M-70 100Q180 220 160 410T480 720"/>
    <path class="road" d="M0 420L430 290 M0 570L430 440 M80 0l210 800 M0 705l430-130"/>
    <text x="260" y="272" transform="rotate(-18 260 272)">${t('PARQUE DEL SOL','SUN PARK')}</text>
    <text x="50" y="530" transform="rotate(-18 50 530)">${t('BARRIO JARDÍN','GARDEN DISTRICT')}</text>
    <text x="235" y="640" transform="rotate(-18 235 640)">${t('LAS FLORES','FLOWER DISTRICT')}</text>
  </svg></div>`;
}
function visibleCenters() { return state.scenario==='empty'?[]:filterCenters(state); }
function mapPage() {
  const results=visibleCenters(), selected=results.find(c=>c.id===state.selected);
  return `<section class="map" aria-label="${t('Mapa ilustrativo de centros','Illustrative center map')}">${mapArt()}
    ${results.map(c=>`<button class="pin ${c.category} ${c.id===state.selected?'selected':''}" style="left:${c.x}%;top:${c.y}%" data-action="pin" data-value="${c.id}" aria-label="${escape(name(c))}" aria-pressed="${c.id===state.selected}">${icon(c.icon)}</button>`).join('')}
    ${state.origin?`<span class="you" role="img" aria-label="${t('Ubicación simulada','Simulated location')}"></span>`:''}
    <div class="map-chrome"><div class="map-toolbar">
      <div class="toolbar-group floating"><button data-action="sheet" data-value="list" aria-label="${t('Abrir lista','Open list')}">${icon('list')}</button><button data-action="sheet" data-value="filters" aria-label="${t('Abrir filtros','Open filters')}">${icon('filter')}</button></div>
      <img class="wordmark" src="/assets/wordmark${state.dark?'-dark':''}.svg" alt="Generosity">
      <button class="floating" data-route="inbox" aria-label="${t('Notificaciones','Notifications')}" style="border-radius:50%">${icon('bell')}</button>
    </div><div class="search-box floating">${icon('search')}<input id="map-search" type="search" value="${escape(state.query)}" placeholder="${t('Centro, colonia o dirección','Center, area or address')}" aria-label="${t('Buscar centro, colonia o dirección','Search center, area or address')}"><button data-action="locate" aria-label="${t('Usar mi ubicación (simulada)','Use my location (simulated)')}">${icon('location')}</button></div>
    <div class="chips" aria-label="${t('Categorías','Categories')}">${categories.map(c=>`<button data-action="category" data-value="${c[0]}" class="${state.category===c[0]?'active':''}" aria-pressed="${state.category===c[0]}">${escape(t(c[1],c[2]))}</button>`).join('')}</div>
    ${state.funding!=='all'?button(`${t('Recursos','Funding')}: ${funding(state.funding)} ×`,'clear-funding','','floating'):''}
    ${state.scenario==='offline'?notice(t('Sin conexión · Última información guardada','Offline · Last saved information')):''}
    </div>
    ${!results.length?`<div class="map-empty floating empty">${icon('search')}<h2>${t('No encontramos centros','No centers found')}</h2><p>${t('Prueba otra categoría o amplía tu búsqueda.','Try another category or broaden your search.')}</p>${button(t('Restablecer filtros','Reset filters'),'reset')}</div>`:''}
    <div class="map-bottom"><span class="scope floating">${results.length} ${results.length===1?t('centro','center'):t('centros','centers')} · ${state.origin?t('Cerca de tu ubicación simulada','Near your simulated location'):t('Zona de exploración','Exploration area')}</span>
    ${selected?`<div class="center-preview floating"><div class="row"><span class="bubble ${selected.category}">${icon(selected.icon)}</span><div class="grow"><h2>${escape(name(selected))} ${badge(selected)}</h2><small>${selected.distance} km · ${escape(funding(selected.funding))}</small></div><button data-action="save" data-value="${selected.id}" aria-label="${t('Guardar centro','Save center')}" aria-pressed="${state.saved.includes(selected.id)}" style="color:${state.saved.includes(selected.id)?'var(--accent)':'inherit'}">${icon('heart')}</button></div>${button(t('Ver centro y necesidades','View center and needs'),'navigate','center','primary','arrow')}</div>`:button(t('Ver centros en lista','See centers in list'),'sheet','list','floating')}
    <span class="map-caption">${t('Mapa ilustrativo · sin GPS ni cartografía real','Illustrative map · no GPS or live cartography')}</span></div></section>`;
}
function render() {
  const focused=document.activeElement?.id, cursor=document.activeElement?.selectionStart;
  const info=routeInfo(state.route);
  app.className=state.route==='map'?'map-route':'';
  app.innerHTML=state.route==='map'?mapPage():state.route==='welcome'?pageContent(state,h):`<header class="screen-header"><button data-action="back" aria-label="${t('Atrás','Back')}">${icon('back')}</button><h1 tabindex="-1">${t(info[1],info[2])}</h1>${state.route==='profile'?button(t('Ajustes','Settings'),'navigate','settings','','settings'):''}</header><div class="page">${pageContent(state,h)}</div>`;
  document.querySelector('#navigation').innerHTML=[['map','Mapa','Map','map'],['needs','Necesidades','Needs','heart'],['help','Ayudar','Help','plus'],['impact','Impacto','Impact','chart'],['profile','Perfil','Profile','person']].map(([route,es,en,i])=>`<button ${route==='help'?'data-action="sheet" data-value="help"':`data-route="${route}"`} class="${state.route===route?'active':''} ${route==='help'?'help-action':''}" ${state.route===route?'aria-current="page"':''} aria-label="${t(es,en)}">${icon(i)}${route==='help'?'':t(es,en)}</button>`).join('');
  document.querySelector('#navigation').hidden=state.route==='welcome';
  document.querySelector('#inventory').innerHTML=`<h2>${t('Explorar pantallas','Explore screens')}</h2>${screens.map(([route,es,en,i])=>`<button class="${state.route===route?'active':''}" data-route="${route}">${icon(i)}${t(es,en)}</button>`).join('')}`;
  document.querySelector('#review-note').innerHTML=`<h2>${t(info[1],info[2])}</h2><p>${t('Explora cómo encontrar centros, coordinar ayuda y seguir su impacto. Elige una función del menú o toca los controles de la app.','Explore how to find centers, coordinate help and follow its impact. Choose a feature from the menu or tap the app controls.')}</p><p>${t('Este recorrido permite conocer las funcionalidades actuales y previstas. Las acciones no envían solicitudes, mensajes ni pagos.','This tour presents current and planned capabilities. Actions do not send requests, messages or payments.')}</p><p><a href="/ayudar/">${t('Encontrar dónde ayudar','Find ways to help')}</a></p><p><a href="/registrar-sede/">${t('Registrar una sede','Register a center')}</a></p>${state.route==='map'?`<label for="scenario">${t('Explora el mapa','Explore the map')}</label><select id="scenario"><option value="normal">${t('Con resultados','With results')}</option><option value="empty">${t('Sin resultados','No results')}</option><option value="offline">${t('Sin conexión','Offline')}</option></select>`:''}`;
  if(document.querySelector('#scenario')) document.querySelector('#scenario').value=state.scenario;
  document.querySelector('#prototype-label').textContent=t('Recorrido interactivo','Interactive tour');
  document.querySelector('#language').textContent=t('EN','ES');
  document.querySelector('#language').ariaLabel=t('Cambiar idioma','Change language');
  document.querySelector('#phone').ariaLabel=t('Recorrido de Generosity','Generosity tour');
  document.querySelector('#navigation').ariaLabel=t('Navegación principal','Main navigation');
  document.querySelector('#inventory').ariaLabel=t('Funcionalidades de Generosity','Generosity features');
  document.documentElement.lang=state.language;
  document.title=t('Generosity · Explora la app','Generosity · Explore the app');
  document.querySelector('.tour-brand a').ariaLabel=t('Generosity · Inicio','Generosity · Home');
  document.querySelector('#theme').ariaLabel=t('Cambiar apariencia','Change appearance');
  document.querySelector('#text-size').ariaLabel=t('Ampliar texto','Enlarge text');
  document.querySelector('#screens-button').ariaLabel=t('Pantallas','Screens');
  if(focused==='map-search') { const input=document.querySelector('#map-search'); input?.focus(); if(cursor!==null) input?.setSelectionRange(cursor,cursor); }
}
function go(route,back=false) {
  if(!screens.some(s=>s[0]===route)) return;
  if(sheet.open) sheet.close();
  if(!back&&route!==state.route) state.stack.push({route:state.route,scroll:app.scrollTop});
  state.route=route; render(); app.scrollTop=0;
  app.querySelector('h1')?.focus({preventScroll:true});
}
function toast(message=t('Solo simulación: no se enviaron datos.','Simulation only: no data was sent.')) {
  const target=document.querySelector('#toast'); target.textContent=message; target.classList.add('visible');
  clearTimeout(toastTimer); toastTimer=setTimeout(()=>target.classList.remove('visible'),4500);
}
function showSheet(kind) {
  let title='',content='';
  switch(kind) {
    case 'list': title=t('Centros en esta zona','Centers in this area'); content=`<small>${t('La misma búsqueda y filtros del mapa','Same search and filters as the map')}</small>${visibleCenters().map(c=>centerRow(c,true)).join('')||notice(t('Sin resultados con estos filtros.','No results with these filters.'))}`; break;
    case 'filters': title=t('Filtrar centros','Filter centers'); content=`<fieldset><legend>${t('Categoría','Category')}</legend>${categories.map(c=>`<label class="check"><input type="radio" name="category" value="${c[0]}" ${state.category===c[0]?'checked':''}>${t(c[1],c[2])}</label>`).join('')}</fieldset><label class="label">${t('Origen de los recursos','Funding source')}<select id="funding"><option value="all">${t('Cualquiera','Any')}</option>${['public','private','mixed'].map(c=>`<option value="${c}" ${state.funding===c?'selected':''}>${funding(c)}</option>`).join('')}</select></label>${link('referral',t('¿Buscas un servicio de atención?','Looking for an intake service?'),t('Alimentos, salud y orientación','Food, health and guidance'))}${link('coverage',t('Explorar cobertura territorial','Explore service coverage'))}${link('businesses',t('Ver negocios que ayudan','See businesses that help'))}${button(t('Mostrar resultados','Show results'),'apply-filters','','primary')}`;break;
    case 'help': title=t('¿Cómo quieres ayudar?','How would you like to help?'); content=`${link('needs',t('Elegir una necesidad','Choose a need'),t('Apoyo solicitado por un centro','Help requested by a center'),'gift')}${button(t('Registrar ayuda personal','Record personal help'),'sheet','personal','secondary','heart')}${notice(t('Las ayudas personales se guardan como autodeclaradas; no generan puntos validados.','Personal help is self-reported and does not earn validated points.'))}`; break;
    case 'personal': title=t('Ayuda personal','Personal help'); content=`<form class="stack" data-form="personal">${field(t('¿Qué hiciste?','What did you do?'))}${field(t('Fecha','Date'),'date')}${field(t('Tiempo dedicado (horas)','Time spent (hours)'),'number')}${button(t('Guardar','Save'),'submit','','primary')}</form>`;break;
    case 'phone': title=t('Verificar celular','Verify mobile number'); content=`${notice(t('Se solicitará para crear grupos o eventos. No es necesario para explorar ni para pedir ayuda.','Required to create groups or events. Not needed to browse or seek help.'))}<form class="stack" data-form="phone">${field(t('Número con código de país','Number with country code'),'tel','+52…')}${button(t('Ver siguiente paso','Preview next step'),'submit','','primary')}</form>`;break;
    case 'phone-code': title=t('Código de verificación','Verification code'); content=`${notice(t('Vista de diseño: no se envió ningún SMS. El proveedor aún debe configurarse.','Design view: no SMS was sent. A provider must still be configured.'))}${field(t('Código','Code'),'text','••••••',false)}${button(t('Volver a configuración','Back to settings'),'navigate','settings')}`;break;
    case 'delete': title=t('Eliminar cuenta','Delete account'); content=`<p>${t('Perderás el acceso a tu cuenta. Antes de confirmar se explicarán los datos que deban conservarse y sus plazos.','You will lose access to your account. Required retention and its duration will be explained before confirmation.')}</p>${notice(t('Aquí no se elimina ninguna cuenta: es una confirmación de muestra.','No account is deleted here: this is a sample confirmation.'))}${button(t('Cancelar','Cancel'),'close')}${button(t('Simular confirmación','Simulate confirmation'),'simulate','','secondary danger')}`;break;
    case 'share': title=t('Compartir una oportunidad','Share an opportunity'); content=`<p>${t('Un enlace abrirá la ficha pública en la web y permitirá participar sin instalar la app.','A link will open the public web page and allow participation without installing the app.')}</p>${link('share',t('Vista previa del enlace','Preview link'))}${notice(t('No se crea ni se envía un enlace público desde este prototipo.','This prototype does not create or send a public link.'))}`;break;
    case 'review': title=t('Tu experiencia','Your experience'); content=`${notice(t('Disponible tras completar una ayuda validada por este centro. La publicación requiere moderación.','Available after completing help validated by this center. Publication requires moderation.'))}<form class="stack" data-form="review">${choice(t('Valoración','Rating'),['5 ★','4 ★','3 ★','2 ★','1 ★'])}<label class="label">${t('Reseña','Review')}<textarea required maxlength="500"></textarea></label>${button(t('Enviar a revisión (simulado)','Submit for review (simulated)'),'submit','','primary')}</form>`;break;
    case 'organizer': title=t('Organiza una campaña','Organize a campaign'); content=`${notice(t('El nivel de ayuda validada y los puntos necesarios se configurarán antes del lanzamiento.','Required validated help and points will be configured before launch.'))}${link('history',t('Consultar mis ayudas validadas','View my validated help'))}${button(t('Verificar mi celular','Verify my mobile number'),'sheet','phone')}${button(t('Ver una campaña de muestra','View a sample campaign'),'navigate','campaign','primary')}`;break;
    case 'certificate': title=t('Reconocimiento','Recognition'); content=`<div class="certificate">${icon('award')}<small>GENEROSITY · ${t('MUESTRA SIN VALIDEZ','INVALID SAMPLE')}</small><h2>${t('Constancia de participación','Certificate of participation')}</h2><p>Lucía Torres</p><strong>${t('6 horas de voluntariado','6 volunteer hours')}</strong><p>${t('Campaña Jardines Vivos · noviembre de 2026','Living Gardens campaign · November 2026')}</p></div>${notice(t('La versión funcional ofrecerá PDF, badge y verificación del emisor. Esto no es un comprobante fiscal.','The functional version will offer a PDF, badge and issuer verification. This is not a tax document.'))}`;break;
    case 'context': title=t('Participar como','Participate as'); content=`${button(t('Yo · Lucía Torres','Me · Lucía Torres'),'context','person')}${button(t('Organización · Manos Unidas','Organization · Helping Hands'),'context','organization')}${notice(t('El centro conserva la autoridad de validar la ayuda. La actividad se cuenta una sola vez.','The center retains authority to validate help. Activity is counted once.'))}`;break;
    case 'editor': title=t('Nueva necesidad','New need'); content=`<form class="stack" data-form="need">${field(t('Título','Title'))}${choice(t('Tipo de ayuda','Help type'),[t('En especie','Goods'),t('Tiempo','Time')])}${field(t('Cantidad','Quantity'),'number')}${field(t('Unidad','Unit'),'text',t('kg, piezas, horas…','kg, items, hours…'))}${field(t('Fecha límite','Deadline'),'date')}<label class="label">${t('Descripción','Description')}<textarea required></textarea></label>${button(t('Vista previa de publicación','Preview publication'),'submit','','primary')}</form>`;break;
    case 'screens':title=t('Todas las pantallas','All screens');content=`<p>${t('Descubre las funciones actuales y previstas. Este recorrido no envía solicitudes ni pagos.','Discover current and planned features. This tour does not send requests or payments.')}</p>`+screens.map(s=>link(s[0],t(s[1],s[2]),'',s[3])).join('');break;
    default:return;
  }
  sheet.className=kind==='list'?'list-sheet':'';
  sheet.innerHTML=`<div class="sheet-head"><h2>${title}</h2><button data-action="close" aria-label="${t('Cerrar','Close')}">${icon('close')}</button></div><div class="sheet-body">${content}</div>`;
  if(!sheet.open) sheet.showModal();
  sheet.scrollTop=0;
}
function appearance() {
  for(const [key,pair] of Object.entries(tokens.colors)) document.documentElement.style.setProperty(`--${key}`,pair[state.dark?1:0]);
  for(const [key,value] of Object.entries(tokens.radii)) document.documentElement.style.setProperty(`--${key}`,`${value}px`);
  document.documentElement.style.colorScheme=state.dark?'dark':'light'; render();
}
document.addEventListener('click',event=>{
  const target=event.target.closest('button'); if(!target) return;
  if(target.dataset.route) { go(target.dataset.route);return; }
  const value=target.dataset.value;
  switch(target.dataset.action) {
    case 'navigate':go(value);break;
    case 'back': {const previous=state.stack.pop()||{route:'map',scroll:0};go(previous.route,true);app.scrollTop=previous.scroll;break;}
    case 'sheet':showSheet(value);break;
    case 'close':sheet.close();break;
    case 'pin':case 'select':state.selected=value;if(sheet.open)sheet.close();render();break;
    case 'center':state.selected=value;go('center');break;
    case 'offer':state.selected=value;go('offer');break;
    case 'save':state.saved=toggleSaved(state.saved,value);render();break;
    case 'category':state.category=value;state.selected=visibleCenters()[0]?.id||'';render();break;
    case 'clear-funding':state.funding='all';render();break;
    case 'apply-filters':state.category=sheet.querySelector('[name=category]:checked').value;state.funding=sheet.querySelector('#funding').value;state.selected=visibleCenters()[0]?.id||'';sheet.close();render();break;
    case 'locate':state.origin=true;state.query='';render();toast(t('Ubicación simulada. La app real pedirá el permiso del sistema.','Simulated location. The real app will request system permission.'));break;
    case 'reset':state.category='all';state.funding='all';state.query='';state.scenario='normal';state.selected='c1';render();break;
    case 'mode':state.mode=value;render();break;
    case 'join':state.joined=!state.joined;render();toast();break;
    case 'meal':state.meal=value;render();toast();break;
    case 'context':state.organization=value==='organization';sheet.close();render();toast();break;
    case 'rank':state.individualRank=!state.individualRank;render();break;
    case 'read':state.read=true;render();break;
    case 'cancel-offer':state.offer=false;render();toast();break;
    case 'simulate':if(sheet.open)sheet.close();toast();break;
    case 'submit':target.closest('form')?.requestSubmit();break;
  }
});
document.addEventListener('input',event=>{
  if(event.target.id==='map-search') {state.query=event.target.value;const results=visibleCenters();if(!results.some(c=>c.id===state.selected))state.selected=results[0]?.id||'';render();}
});
document.addEventListener('change',event=>{if(event.target.id==='scenario'){state.scenario=event.target.value;go('map');}});
document.addEventListener('submit',event=>{
  event.preventDefault();
  const kind=event.target.dataset.form;
  if(kind==='phone') {showSheet('phone-code');return;}
  if(kind==='offer') {const inputs=event.target.querySelectorAll('input');state.offer={center:state.selected,quantity:inputs[0].value,unit:event.target.querySelector('select').value,date:inputs[1].value};go('history');}
  else if(kind==='referral') go('case');
  else {if(sheet.open)sheet.close();toast();}
});
document.querySelector('#theme').onclick=()=>{state.dark=!state.dark;appearance();};
document.querySelector('#language').onclick=()=>{state.language=state.language==='es'?'en':'es';try { localStorage.setItem('generosity.locale',state.language); } catch (_) { /* Keep the current choice when storage is unavailable. */ } if(sheet.open)sheet.close();render();};
document.querySelector('#text-size').onclick=event=>{const large=document.body.classList.toggle('large');event.currentTarget.setAttribute('aria-pressed',String(large));};
document.querySelector('#screens-button').onclick=()=>showSheet('screens');
try {
  const response=await fetch('/assets/app-tokens.json?v=722201d5a8f2');if(!response.ok)throw new Error('Design tokens unavailable');tokens=await response.json();appearance();
} catch(error) {
  app.innerHTML='<p role="alert">No se pudo cargar el recorrido. Recarga la página para intentarlo de nuevo. / Unable to load the tour. Reload the page to try again.</p>';console.error(error);
}
