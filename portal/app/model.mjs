// Fixture-only UI exploration. Nothing here is sent to an account or backend.
// Route, bilingual name and icon.
export const screens = [
  ['map', 'Mapa', 'Map', 'map'],
  ['needs', 'Necesidades', 'Needs', 'heart'],
  ['center', 'Ficha del centro', 'Center profile', 'home'],
  ['offer', 'Quiero ayudar', 'Offer help', 'gift'],
  ['history', 'Mis aportaciones', 'My contributions', 'clock'],
  ['impact', 'Mi impacto', 'My impact', 'chart'],
  ['achievements', 'Logros y certificados', 'Achievements & certificates', 'award'],
  ['regions', 'Ayuda por región', 'Regional helping', 'map'],
  ['saved', 'Centros guardados', 'Saved centers', 'heart'],
  ['reviews', 'Reseñas', 'Reviews', 'star'],
  ['inbox', 'Notificaciones', 'Notifications', 'bell'],
  ['messages', 'Conversaciones', 'Conversations', 'message'],
  ['events', 'Eventos', 'Events', 'calendar'],
  ['event', 'Detalle del evento', 'Event details', 'calendar'],
  ['campaign', 'Campañas y equipos', 'Campaigns & teams', 'people'],
  ['meals', 'Comidas coordinadas', 'Meal train', 'meal'],
  ['food', 'Rescate de alimentos', 'Food rescue', 'meal'],
  ['referral', 'Canalizar una necesidad', 'Refer someone for help', 'shield'],
  ['case', 'Seguimiento privado', 'Private follow-up', 'shield'],
  ['assessment', 'Levantamiento de necesidades', 'Needs assessment', 'clipboard'],
  ['coverage', 'Cobertura territorial', 'Service coverage', 'layers'],
  ['organization', 'Mi organización', 'My organization', 'people'],
  ['businesses', 'Negocios que ayudan', 'Businesses that help', 'shop'],
  ['funding', 'Sostener Generosity', 'Support Generosity', 'chart'],
  ['donate', 'Donación económica', 'Donate money', 'gift'],
  ['tax', 'Comprobantes fiscales', 'Tax documents', 'clipboard'],
  ['staff', 'Espacio del centro', 'Center workspace', 'home'],
  ['onboarding', 'Registrar una sede', 'Register a center', 'pin'],
  ['operations', 'Revisión y conectores', 'Review & connectors', 'clipboard'],
  ['share', 'Participar desde un enlace', 'Join from a link', 'share'],
  ['assisted', 'Participación asistida', 'Assisted participation', 'people'],
  ['profile', 'Perfil', 'Profile', 'person'],
  ['settings', 'Configuración', 'Settings', 'settings'],
  ['welcome', 'Bienvenida', 'Welcome', 'heart'],
];

export const centers = [
  {id: 'c1', name: 'Casa Comunitaria Jacaranda', en: 'Jacaranda Community House', category: 'food', funding: 'mixed', address: 'Barrio del Sol · Coyoacán', distance: 0.4, x: 38, y: 46, verified: true, icon: 'meal'},
  {id: 'c2', name: 'Centro de Salud Horizonte', en: 'Horizonte Health Center', category: 'health', funding: 'public', address: 'Barrio del Sol · Coyoacán', distance: 0.9, x: 72, y: 35, verified: true, icon: 'cross'},
  {id: 'c3', name: 'Hogar de Mayores Los Olivos', en: 'Los Olivos Senior Home', category: 'seniors', funding: 'private', address: 'Colonia Las Flores · Coyoacán', distance: 1.2, x: 68, y: 64, verified: false, icon: 'people'},
  {id: 'c4', name: 'Banco de Sangre Encuentro', en: 'Encuentro Blood Bank', category: 'blood', funding: 'public', address: 'Colonia Las Flores · Coyoacán', distance: 1.6, x: 25, y: 72, verified: false, icon: 'drop'},
  {id: 'c5', name: 'Casa Hogar Semillas', en: 'Semillas Children’s Home', category: 'children', funding: 'private', address: 'Barrio Jardín · Coyoacán', distance: 2.1, x: 22, y: 33, verified: true, icon: 'home'},
  {id: 'c6', name: 'Taller Aprender Juntos', en: 'Learn Together Workshop', category: 'training', funding: 'mixed', address: 'Barrio Jardín · Coyoacán', distance: 2.4, x: 79, y: 78, verified: false, icon: 'award'},
];

export const categories = [
  ['all','Todas','All','layers'], ['food','Comedores','Food','meal'], ['health','Salud','Health','cross'],
  ['blood','Sangre','Blood','drop'], ['seniors','Mayores','Seniors','people'],
  ['children','Casas hogar','Children','home'], ['training','Capacitación','Training','award'],
];
export const needs = [
  {id:'n1', center:'c1', title:'Alimentos no perecederos', en:'Non-perishable food', description:'Arroz, frijol y aceite para el comedor.', descriptionEn:'Rice, beans and cooking oil for the kitchen.', priority:'urgent', icon:'gift', amount:'24 / 40 kg'},
  {id:'n2', center:'c3', title:'Una tarde de compañía', en:'An afternoon together', description:'Lectura y conversación con personas mayores.', descriptionEn:'Reading and conversation with older adults.', priority:'normal', icon:'people', amount:'3 / 6'},
  {id:'n3', center:'c4', title:'Jornada de donación de sangre', en:'Blood donation day', description:'Consulta los requisitos antes de asistir.', descriptionEn:'Check the requirements before attending.', priority:'urgent', icon:'drop', amount:'8 / 20'},
];

export const normalize = value => value.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase().trim();
export function filterCenters({query='', category='all', funding='all', savedOnly=false, saved=[]}={}) {
  const q=normalize(query);
  return centers.filter(c => (category==='all'||c.category===category) && (funding==='all'||c.funding===funding)
    && (!savedOnly||saved.includes(c.id)) && normalize(`${c.name} ${c.en} ${c.address}`).includes(q));
}
export function routeInfo(route) { return screens.find(s=>s[0]===route) || screens[0]; }
export function toggleSaved(saved, id) {
  if (!centers.some(c=>c.id===id)) return saved;
  return saved.includes(id) ? saved.filter(v=>v!==id) : [...saved,id];
}
