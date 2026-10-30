import {
  createProducts,
  seedProducts,
} from "./catalog.mjs";

async function main() {
  const args = process.argv.slice(2);

  let projectId;
  let dryRun = false;
  let overwrite = false;

  for (let index = 0; index < args.length; index++) {
    const arg = args[index];

    if (arg === "--dry-run") {
      dryRun = true;

    } else if (arg === "--overwrite") {
      overwrite = true;

    } else if (
      arg === "--project" &&
      args[index + 1] &&
      !args[index + 1].startsWith("--")
    ) {
      projectId = args[++index];

    } else {
      throw new Error(
        `Argumento inválido: ${arg}. ` +
        "Usa --project ID [--overwrite] o --dry-run."
      );
    }
  }

  if (dryRun) {
    const products = createProducts();

    console.log(
      JSON.stringify(
        {
          count: products.length,
          first: products[0],
          last: products.at(-1),
        },
        null,
        2,
      )
    );

    return;
  }

  if (!projectId) {
    throw new Error(
      "Indica el ID de tu proyecto Firebase con --project ID."
    );
  }

  if (process.env.FIRESTORE_EMULATOR_HOST) {
    console.log(
      `Destino: emulador ${process.env.FIRESTORE_EMULATOR_HOST}; ` +
      `proyecto: ${projectId}`
    );
  } else {
    console.log(
      `Destino: Firestore remoto; proyecto: ${projectId}`
    );
  }

  const {
    initializeApp,
    applicationDefault,
    deleteApp,
  } = await import("firebase-admin/app");

  const {
    getFirestore,
  } = await import("firebase-admin/firestore");

  const app = initializeApp(
    process.env.FIRESTORE_EMULATOR_HOST
      ? { projectId }
      : {
          projectId,
          credential: applicationDefault(),
        }
  );

  const db = getFirestore(app);

  try {
    const result = await seedProducts(
      db,
      { overwrite },
    );

    console.log(
      `Finalizado: ${result.written} escritos; ` +
      `${result.skipped} existentes conservados.`
    );
  } finally {
    try {
      await db.terminate();
    } finally {
      await deleteApp(app);
    }
  }
}

main().catch((error) => {
  console.error(`Seeder: ${error.message}`);
  process.exitCode = 1;
});