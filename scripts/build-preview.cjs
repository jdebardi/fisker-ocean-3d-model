const path=require('node:path');
const esbuild=require('esbuild');
const root=path.resolve(__dirname,'..');
esbuild.buildSync({entryPoints:[path.join(root,'preview/src/main.js')],outfile:path.join(root,'preview/app.js'),bundle:true,minify:true,format:'iife',target:['es2020'],legalComments:'eof'});
console.log('Built preview/app.js');
