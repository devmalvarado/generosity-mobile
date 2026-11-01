const {test,expect}=require('@playwright/test');
const api='https://api.generosity.global';
const need={id:'need-a',title:{es:'Alimentos',en:'Food'},description:{es:'Paquetes',en:'Packs'},helpMethods:['goods'],urgent:false,status:'published',quantityRequired:20,quantityCommitted:2,unit:'paquetes',version:1};
const pageOf=data=>({data,current_page:1,last_page:1});
async function fixture(page){
 await page.route(api+'/auth/session',r=>r.fulfill({json:{user:{id:42,name:'María'},csrf_token:'csrf-test'}}));
 await page.route(api+'/api/v1/staff/centers',r=>r.fulfill({json:{centers:[{id:'center-a',name:{es:'Centro Ñ',en:'Center'}}]}}));
 await page.route(api+'/api/v1/staff/centers/center-a/needs?*',r=>r.fulfill({json:pageOf([need])}));
 await page.route(api+'/api/v1/staff/centers/center-a/contributions?*',r=>r.fulfill({json:pageOf([{id:'offer-a',user_id:7,title:{es:'Alimentos'},status:'offered',version:1,method:'goods',quantity:3,notes:'<img src=x onerror=alert(1)>'}])}));
 await page.goto('/#center-workspace');await page.locator('#idioma').selectOption('es');await expect(page.locator('#staff-needs button')).toBeEnabled();
}
test('staff edits preserve translations and send CSRF, version and idempotency',async({page})=>{
 await fixture(page);await page.locator('#staff-needs button').click();await page.locator('#need-title-es').fill('Medicinas para niñas');let sent;
 await page.route(api+'/api/v1/staff/centers/center-a/needs/need-a',r=>{sent=r.request();return r.fulfill({json:{...need,version:2}});});
 await page.locator('#staff-need-form button').click();await expect(page.locator('#staff-status')).toContainText('confirmó');
 expect(sent.method()).toBe('PUT');expect(sent.headers()['x-csrf-token']).toBe('csrf-test');expect(sent.headers()['idempotency-key']).toMatch(/^[a-f0-9-]{36}$/);expect(sent.postDataJSON()).toMatchObject({version:1,title:{es:'Medicinas para niñas',en:'Food'}});await expect(page.locator('#staff-contributions img')).toHaveCount(0);
});
test('unknown result survives reload with same payload and operation key',async({page})=>{
 await fixture(page);let calls=[];await page.route(api+'/api/v1/staff/centers/center-a/needs',r=>{calls.push(r.request());return calls.length===1?r.abort():r.fulfill({json:need});});
 await page.locator('#need-title-es').fill('Agua');await page.locator('#need-description-es').fill('Botellas');await page.locator('#staff-need-form button').click();await expect(page.locator('#staff-retry')).toBeVisible();await expect(page.locator('#need-title-es')).toHaveValue('Agua');
 await page.reload();await expect(page.locator('#staff-retry')).toBeEnabled();await page.locator('#staff-retry').click();await expect(page.locator('#staff-retry')).toBeHidden();expect(calls.length).toBe(2);expect(calls[0].headers()['idempotency-key']).toBe(calls[1].headers()['idempotency-key']);expect(calls[0].postData()).toBe(calls[1].postData());
});
test('conflict preserves draft and staff transition is versioned',async({page})=>{
 await fixture(page);await page.route(api+'/api/v1/staff/centers/center-a/needs/need-a',r=>r.fulfill({status:409,json:{error:{code:'version_conflict'}}}));await page.locator('#staff-needs button').click();await page.locator('#need-title-es').fill('Mi cambio');await page.locator('#staff-need-form button').click();await expect(page.locator('#staff-status')).toContainText('versión actual');await expect(page.locator('#need-title-es')).toHaveValue('Mi cambio');let sent;
 await page.route(api+'/api/v1/contributions/offer-a/transition',r=>{sent=r.request().postDataJSON();return r.fulfill({json:{}});});await page.locator('#staff-contributions button').click();await expect(page.locator('#staff-status')).toContainText('confirmó');expect(sent).toEqual({action:'confirm',version:1});
 await page.route(api+'/api/v1/staff/centers',r=>r.fulfill({json:{centers:[]}}));await page.locator('#staff-reload').click();await expect(page.locator('#staff-content')).toBeHidden();await expect(page.locator('#staff-status')).toContainText('no tiene centros');
});
test('account switch clears private draft',async({page})=>{
 await fixture(page);await page.locator('#need-title-es').fill('Borrador privado');await page.route(api+'/auth/session',r=>r.fulfill({json:{user:{id:99,name:'Otra cuenta'},csrf_token:'other'}}));await page.locator('#account-retry').click();await expect(page.locator('#need-title-es')).toHaveValue('');expect(await page.evaluate(()=>sessionStorage.getItem('generosity.staff.pending.42'))).toBeNull();
});
test('entered actual duration survives session refresh and is submitted explicitly',async({page})=>{
 await fixture(page);
 await page.route(api+'/api/v1/staff/centers/center-a/contributions?*',r=>r.fulfill({json:pageOf([{id:'time-a',user_id:7,title:{en:'Tutoring'},status:'confirmed',version:2,method:'time',minutes:90}])}));
 await page.locator('#staff-reload').click();
 await page.locator('#staff-contributions input').fill('75');
 await page.locator('#account-retry').click();
 await expect(page.locator('#staff-contributions input')).toHaveValue('75');
 let sent;
 await page.route(api+'/api/v1/contributions/time-a/transition',r=>{sent=r.request().postDataJSON();return r.fulfill({json:{}});});
 await page.locator('#staff-contributions button').click();
 await expect(page.locator('#staff-status')).toContainText('confirmó');
 expect(sent).toEqual({action:'fulfill',version:2,actual_minutes:75});
});
test('storage deletion failure does not retain another accounts private view',async({page})=>{
 await fixture(page);await page.locator('#need-title-es').fill('Borrador privado');
 await page.evaluate(()=>{Storage.prototype.removeItem=function(){throw new Error('Storage unavailable');};});
 await page.route(api+'/auth/session',r=>r.fulfill({json:{user:null,csrf_token:'guest'}}));
 await page.locator('#account-retry').click();
 await expect(page.locator('#center-workspace')).toBeHidden();
 await expect(page.locator('#need-title-es')).toHaveValue('');
 await expect(page.locator('#staff-contributions')).toBeEmpty();
});
test('workspace fits narrow screens with large Spanish and English text',async({page})=>{
 await fixture(page);for(const width of [320,360,390,768,1280]){await page.setViewportSize({width,height:900});for(const lang of ['es','en']){await page.locator('#idioma').selectOption(lang);await page.evaluate(()=>document.documentElement.style.fontSize='200%');const size=await page.evaluate(()=>({w:innerWidth,scroll:document.documentElement.scrollWidth}));expect(size.scroll).toBeLessThanOrEqual(size.w+1);await expect(page.locator('#staff-need-form button')).toBeVisible();await page.evaluate(()=>document.documentElement.style.fontSize='');}}
});
