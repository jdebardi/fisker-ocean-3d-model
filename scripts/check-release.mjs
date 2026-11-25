import fs from 'node:fs';import path from 'node:path';import assert from 'node:assert/strict';import{fileURLToPath}from'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const contract=JSON.parse(read('model-contract.json'));
assert.equal(read('VERSION').trim(),'3.0.0');assert.equal(JSON.parse(read('package.json')).version,'3.0.0');assert.equal(contract.asset_revision,'3.0.0');assert.equal(contract.version,'2.0');
for(const name of ['LICENSE.md','ATTRIBUTION.md','LICENSES/CC-BY-4.0.txt','LICENSES/MIT.txt','LICENSES/THREE-MIT.txt','AGENTS.md','llms.txt','media/showcase.mp4','preview/app.js'])assert(fs.existsSync(path.join(root,name)),name+' missing');
assert(read('LICENSES/CC-BY-4.0.txt').includes('Section 8 -- Interpretation.'));
const required=['VehicleYaw','Glass_Windscreen','Glass_Quarter_L','Glass_Quarter_R','Hinge_Tailgate','Slide_Sunroof_Front'];
for(const key of ['FL','FR','RL','RR'])required.push('Hinge_Door_'+key,'Frame_Door_'+key,'Slide_Handle_'+key,'Roll_'+key);
required.push('Steer_FL','Steer_FR');
let firstNames;
for(const file of ['ocean.glb','ocean-lite.glb']){
 const b=fs.readFileSync(path.join(root,'models',file));assert.equal(b.readUInt32LE(0),0x46546c67);assert.equal(b.readUInt32LE(8),b.length);
 const j=JSON.parse(b.subarray(20,20+b.readUInt32LE(12)).toString());const meta=j.asset.extras.digitalTwin;assert.equal(meta.assetRevision,'3.0.0');assert.equal(meta.contractVersion,'2.0');
 const nodes=new Map(j.nodes.map((n,i)=>[n.name,{...n,index:i}])),parent=new Map();j.nodes.forEach((n,i)=>(n.children||[]).forEach(c=>parent.set(c,i)));
 for(const n of required)assert(nodes.has(n),file+' missing '+n);
 for(const n of ['Glass_Windscreen','Glass_Quarter_L','Glass_Quarter_R'])assert.equal(parent.get(nodes.get(n).index),nodes.get('VehicleYaw').index,'fixed glass parent');
 for(const key of ['FL','FR','RL','RR'])assert.equal(parent.get(nodes.get('Frame_Door_'+key).index),nodes.get('Hinge_Door_'+key).index,'door frame parent');
 const mats=new Set(j.materials.map(m=>m.name));for(const n of ['Paint_Body','Mirror_Glass','Trim_Dark','Mat_Indicator_L','Mat_Indicator_R','Mat_Reverse_L','Mat_Reverse_R','Mat_SolarCells','Mat_SolarPower'])assert(mats.has(n),file+' missing material '+n);
 assert.equal(meta.wheelOptions.length,6);assert.equal(meta.paintPresets.length,14);assert.equal(meta.openings.fixed_windscreen_node,'Glass_Windscreen');
 const all=new Set(required);if(firstNames)assert.deepEqual(all,firstNames);else firstNames=all;
 console.log(file+': release metadata, required controls, fixed glass and frames PASS');
}
function files(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.name==='node_modules'||e.name==='.git'?[]:e.isDirectory()?files(path.join(dir,e.name)):[path.join(dir,e.name)]);}
for(const file of files(root)){
 const rel=path.relative(root,file);
 if(!/\.(md|txt|json|js|cjs|mjs|kt|html|command|bat)$/.test(file))continue;
 const s=fs.readFileSync(file,'utf8');
 if(file.endsWith('.md'))for(const m of s.matchAll(/!?\[[^\]]*\]\(([^)]+)\)/g)){
  const link=m[1].replace(/^<|>$/g,'').split('#')[0];if(!link||/^[a-z]+:/i.test(link))continue;
  assert(fs.existsSync(path.resolve(path.dirname(file),decodeURIComponent(link))),rel+' broken link '+link);
 }
}
assert(read('preview/src/main.js').includes("location.protocol==='file:'"));assert(!read('index.html').includes('assets/ocean.glb'));
console.log('PASS: release structure, local documentation links and file-loading entry point');
