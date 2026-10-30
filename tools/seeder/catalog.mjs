export function createProducts() {
  const developers = [
    "Nebula Forge",
    "Pixel Jaguar",
    "Aurora Byte",
    "Sakura Circuit",
    "Andes Interactive",
    "Emerald Owl Games",
    "Solaris Works",
    "Crimson Kraken",
    "Nordic Lantern",
    "Quetzal Labs",
  ];

  const themes = [
    "Crónicas",
    "Horizonte",
    "Leyendas",
    "Reinos",
    "Circuito",
    "Guardianes",
    "Ecos",
    "Expedición",
    "Arena",
    "Misterios",
  ];

  const worlds = [
    "de Aether",
    "del Jaguar",
    "Neón",
    "del Norte",
    "Solar",
    "Abisal",
    "de Jade",
    "Andina",
    "Estelar",
    "Esmeralda",
  ];

  const genres = [
    "aventura",
    "acción",
    "estrategia",
    "carreras",
    "rompecabezas",
    "simulación",
    "rol",
    "deportes",
    "supervivencia",
    "plataformas",
  ];

  return Array.from({ length: 500 }, (_, index) => {
    const number = index + 1;

    return {
      id: `game-${String(number).padStart(3, "0")}`,

      name:
        `${themes[index % 10]} ` +
        `${worlds[Math.floor(index / 10) % 10]} #${number}`,

      description:
        `Videojuego de ${genres[index % 10]} ` +
        `desarrollado por ${developers[index % 10]}.`,

      price: (999 + ((index * 7) % 60) * 100) / 100,

      developerId: `dev${String(index % 10 + 1).padStart(2, "0")}`,

      imageUrl:
        `https://picsum.photos/seed/gamestore-${number}/600/400`,

      isAvailable: number % 7 !== 0,

      stock: number % 7 === 0
        ? 0
        : 1 + ((index * 3) % 25),

      coverColorIndex: index % 10,
    };
  });
}

export async function seedProducts(
  db,
  { overwrite = false } = {},
) {
  const products = createProducts();

  let written = 0;
  let skipped = 0;

  for (
    let offset = 0;
    offset < products.length;
    offset += 200
  ) {
    const group = products.slice(offset, offset + 200);

    const refs = group.map(({ id }) =>
      db.collection("products").doc(id)
    );

    const snapshots = overwrite
      ? []
      : await db.getAll(...refs);

    const batch = db.batch();

    let pending = 0;

    group.forEach(({ id, ...data }, index) => {
      if (!overwrite && snapshots[index].exists) {
        skipped++;
        return;
      }

      if (overwrite) {
        batch.set(refs[index], data);
      } else {
        batch.create(refs[index], data);
      }

      pending++;
    });

    if (pending > 0) {
      await batch.commit();
      written += pending;
    }
  }

  return {
    written,
    skipped,
  };
}