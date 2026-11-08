"""Entry point for the DoraMCU Deep Learning AI service."""

import logging
import sys

from dora_dl.server import app, main

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s: %(message)s",
)

if __name__ == "__main__":
    main()
