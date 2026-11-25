"""Package only files tracked in the exact release commit, not local build output."""
from pathlib import Path
import hashlib,re,shutil,subprocess,zipfile
root=Path(__file__).resolve().parent.parent
version=(root/'VERSION').read_text().strip()
if not re.fullmatch(r'\d+\.\d+\.\d+',version):raise ValueError('VERSION must be a stable semantic version')
out=root/'dist';out.mkdir(exist_ok=True)
archive=out/f'fisker-ocean-3d-model-v{version}.zip'
with archive.open('wb') as stream:
 subprocess.run(['git','archive','--format=zip',f'--prefix=fisker-ocean-3d-model-v{version}/','HEAD'],cwd=root,stdout=stream,check=True)
for source,target in [('media/showcase.mp4','showcase.mp4'),('models/ocean.glb','ocean.glb'),('models/ocean-lite.glb','ocean-lite.glb')]:
 data=subprocess.check_output(['git','show',f'HEAD:{source}'],cwd=root);(out/target).write_bytes(data)
with zipfile.ZipFile(archive) as z:
 if z.testzip() is not None:raise ValueError('Release ZIP integrity failed')
files=[archive,out/'showcase.mp4',out/'ocean.glb',out/'ocean-lite.glb']
(out/'release-SHA256SUMS.txt').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in files))
print(f'Packaged v{version}: '+', '.join(p.name for p in files))
