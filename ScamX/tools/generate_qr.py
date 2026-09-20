"""Generate the two release QR codes after permanent HTTPS URLs exist."""
import argparse
from pathlib import Path
import qrcode


def make(url: str, destination: Path) -> None:
    if not url.startswith("https://"):
        raise SystemExit(f"Refusing non-HTTPS release URL: {url}")
    image = qrcode.make(url)
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)


parser = argparse.ArgumentParser()
parser.add_argument("--website", required=True, help="Permanent public website URL")
parser.add_argument("--android", required=True, help="Permanent APK landing-page or app-store URL")
parser.add_argument("--output", default="release-qr")
args = parser.parse_args()
target = Path(args.output)
make(args.website, target / "scamx-website-qr.png")
make(args.android, target / "scamx-android-qr.png")
print(f"Created release QR codes in {target.resolve()}")
