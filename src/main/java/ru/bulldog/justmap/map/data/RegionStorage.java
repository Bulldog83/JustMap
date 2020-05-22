package ru.bulldog.justmap.map.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;

public class RegionStorage implements AutoCloseable {
	
	private final Long2ObjectLinkedOpenHashMap<RegionFile> cachedRegionFiles = new Long2ObjectLinkedOpenHashMap<>();
	private final File directory;
	
	public RegionStorage(File file) {
		this.directory = file;
	}
	
	private RegionFile getRegionFile(ChunkPos chunkPos) throws IOException {
		long longPos = ChunkPos.toLong(chunkPos.getRegionX(), chunkPos.getRegionZ());
		RegionFile regionFile = (RegionFile) this.cachedRegionFiles.getAndMoveToFirst(longPos);
		
		if (regionFile != null) {
			return regionFile;
		} else {
			if (this.cachedRegionFiles.size() >= 256) {
				((RegionFile) this.cachedRegionFiles.removeLast()).close();
			}

			if (!this.directory.exists()) {
				this.directory.mkdirs();
			}

			File file = new File(this.directory, "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca");
			regionFile = new RegionFile(file, this.directory);
			this.cachedRegionFiles.putAndMoveToFirst(longPos, regionFile);
			
			return regionFile;
		}
	}

	public CompoundTag getTagAt(ChunkPos chunkPos) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataInputStream dataInputStream = regionFile.getChunkInputStream(chunkPos);
		Throwable exception = null;

		CompoundTag chunkTag;
		try {
			if (dataInputStream == null) {
				return null;
			}

			chunkTag = NbtIo.read(dataInputStream);
		} catch (Throwable ex) {
			exception = ex;
			throw ex;
		} finally {
			if (dataInputStream != null) {
				if (exception != null) {
					try {
						dataInputStream.close();
					} catch (Throwable ex2) {
						exception.addSuppressed(ex2);
					}
				} else {
					dataInputStream.close();
				}
			}
		}

		return chunkTag;
	}

	protected void write(ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataOutputStream dataOutputStream = regionFile.getChunkOutputStream(chunkPos);
		Throwable exeption = null;

		try {
			NbtIo.write((CompoundTag) compoundTag, (DataOutput) dataOutputStream);
		} catch (Throwable ex) {
			exeption = ex;
			throw ex;
		} finally {
			if (dataOutputStream != null) {
				if (exeption != null) {
					try {
						dataOutputStream.close();
					} catch (Throwable ex2) {
						exeption.addSuppressed(ex2);
					}
				} else {
					dataOutputStream.close();
				}
			}
		}
	}

	@Override
	public void close() throws Exception {
		ObjectIterator<RegionFile> regionIterator = this.cachedRegionFiles.values().iterator();

		while(regionIterator.hasNext()) {
			RegionFile regionFile = (RegionFile) regionIterator.next();
			regionFile.close();
		}
		
		this.cachedRegionFiles.clear();
	}
}
