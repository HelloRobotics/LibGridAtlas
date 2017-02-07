/*
 * Copyright 2017 HelloRobotics.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package io.github.hellorobotics.lib;

import io.github.hellorobotics.lib.util.ArraySection;
import io.github.hellorobotics.lib.util.Section;
import io.github.hellorobotics.lib.util.SectionIterator;
import io.github.hellorobotics.lib.util.Single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Author: Towdium
 * Date:   04/02/17
 */
public class GridAtlas {
    private int chunkSize;
    private Section<Section<Chunk>> chunks;

    public GridAtlas(int chunkSize) {
        this.chunkSize = chunkSize;
        chunks = new ArraySection<>();
        ArraySection<Chunk> row = new ArraySection<>();
        row.add(new ChunkEmpty(0, 0));
        chunks.add(row);
    }

    protected static int divFlr(int a, int b) {
        return a >= 0 ? a / b : (a + 1) / b - 1;
    }

    public Cell getCell(int x, int y) {
        expandTo(x, y);
        Single<ChunkFilled> c = new Single<>();
        Single<Cell> e = new Single<>();
        int xChunk = divFlr(x, chunkSize);
        int yChunk = divFlr(y, chunkSize);
        getChunkAt(xChunk, yChunk).ifPresent((value) -> c.push(value.generate()));
        c.getValue().ifPresent(chunk -> setChunkAt(xChunk, yChunk, chunk));
        c.getValue().ifPresent(chunk -> e.push(chunk.getCell(x, y)));
        Optional<Cell> oe = e.getValue();
        if (oe.isPresent())
            return oe.get();
        else
            throw new IllegalStateException("Internal error.");
    }

    public int xMin() {
        return xMinChunk() * chunkSize;
    }

    public int xMax() {
        return (xMaxChunk() + 1) * chunkSize - 1;
    }

    public int yMin() {
        return yMinChunk() * chunkSize;
    }

    public int yMax() {
        return (yMaxChunk() + 1) * chunkSize - 1;
    }

    public void updateCell(int x, int y, boolean up) {
        expandTo(x, y);
        int xChunk = divFlr(x, chunkSize);
        int yChunk = divFlr(y, chunkSize);
        getChunkAt(xChunk, yChunk).ifPresent(chunk -> setChunkAt(xChunk, yChunk, chunk.updateCell(x, y, up)));
    }

    protected int xMinChunk() {
        return chunks.start();
    }

    protected int xMaxChunk() {
        return chunks.end();
    }

    protected int yMinChunk() {
        return chunks.isEmpty() ? 0 : chunks.iterator().next().start();
    }

    protected int yMaxChunk() {
        return chunks.isEmpty() ? -1 : chunks.iterator().next().end();
    }

    protected Optional<Chunk> getNeighbourChunk(Chunk c, enumDirection d) {
        switch (d) {
            case EAST:
                return getChunkAt(c.getX() + 1, c.getY());
            case WEST:
                return getChunkAt(c.getX() - 1, c.getY());
            case NORTH:
                return getChunkAt(c.getX(), c.getY() + 1);
            case SOUTH:
                return getChunkAt(c.getX(), c.getY() - 1);
            default:
                return Optional.empty();
        }
    }

    protected Optional<Chunk> getChunkAt(int x, int y) {
        Optional<Section<Chunk>> row = chunks.get(x);
        if (!row.isPresent())
            return Optional.empty();
        else
            return row.get().get(y);
    }

    protected void setChunkAt(int x, int y, Chunk c) {
        Optional<Section<Chunk>> row = chunks.get(x);
        if (!row.isPresent())
            throw new IndexOutOfBoundsException();
        else
            row.get().set(y, c);
    }

    protected void expandTo(int x, int y) {
        if (x <= xMax() && x >= xMin() && y <= yMax() && y >= yMin())
            return;
        int xChunk = divFlr(x, chunkSize);
        int yChunk = divFlr(y, chunkSize);
        int i;
        if (xChunk < (i = xMinChunk())) {
            for (int j = i - xChunk + 1; j > 0; j--) {
                expandX(false);
            }
        } else if (xChunk > (i = xMaxChunk())) {
            for (int j = xChunk - i + 1; j > 0; j--) {
                expandX(true);
            }
        }
        if (yChunk < (i = yMinChunk())) {
            for (int j = i - yChunk + 1; j > 0; j--) {
                expandY(false);
            }
        } else if (yChunk > (i = yMaxChunk())) {
            for (int j = yChunk - i + 1; j > 0; j--) {
                expandY(true);
            }
        }
    }

    protected void expandX(boolean forward) {
        int yMin = yMinChunk();
        int yMax = yMaxChunk();
        int x = forward ? xMaxChunk() + 1 : xMinChunk() - 1;
        ArraySection<Chunk> ret = new ArraySection<>(yMin);
        for (int i = yMin; i <= yMax; i++)
            ret.set(i, new ChunkEmpty(x, i));
        chunks.add(ret, forward);
    }

    protected void expandY(boolean forward) {
        SectionIterator<Section<Chunk>> i = chunks.sectionIterator();
        int y = forward ? yMaxChunk() + 1 : yMinChunk() - 1;
        while (i.hasNext()) {
            i.next().add(new ChunkEmpty(i.previousIndex(), y), forward);
        }
    }

    enum enumDirection {
        EAST, WEST, NORTH, SOUTH, ERROR;

        enumDirection getOpposite() {
            switch (this) {
                case EAST:
                    return WEST;
                case WEST:
                    return EAST;
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
                default:
                    return ERROR;
            }
        }
    }

    public interface Cell {
        List<Cell> getAccessibleCells();

        default double getDistanceTo(Cell c) {
            return Math.pow(Math.pow(getX() - c.getX(), 2.0) + Math.pow(getY() - c.getY(), 2.0), 0.5);
        }

        int getX();

        int getY();
    }

    abstract class Chunk {
        int x;
        int y;

        public Chunk(int x, int y) {
            this.x = x;
            this.y = y;
        }

        abstract ChunkFilled generate();

        abstract Chunk updateCell(int x, int y, boolean up);

        abstract List<Cell> getEmptyBoundaryPoints(enumDirection d);

        abstract Optional<Cell> getEmptyBoundaryPointAt(enumDirection d, int index);

        Optional<Chunk> getChunkAt(enumDirection d) {
            return getNeighbourChunk(this, d);
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }
    }

    class ChunkEmpty extends Chunk {

        public ChunkEmpty(int x, int y) {
            super(x, y);
        }

        @Override
        public List<Cell> getEmptyBoundaryPoints(enumDirection d) {
            return Collections.singletonList(new CellEmpty());
        }

        @Override
        public Optional<Cell> getEmptyBoundaryPointAt(enumDirection d, int index) {
            return Optional.of(new CellEmpty());
        }

        @Override
        ChunkFilled generate() {
            return new ChunkFilled(this.x, this.y);
        }

        @Override
        Chunk updateCell(int x, int y, boolean up) {
            return up ? new ChunkFilled(this.x, this.y).updateCell(x, y, true) : this;
        }

        class CellEmpty implements Cell {
            @Override
            public List<Cell> getAccessibleCells() {
                ArrayList<Cell> ret = new ArrayList<>(chunkSize * 4);
                for (enumDirection d : enumDirection.values()) {
                    GridAtlas.this.getNeighbourChunk(ChunkEmpty.this, d).
                            ifPresent(chunk -> ret.addAll(chunk.getEmptyBoundaryPoints(d.getOpposite())));
                }
                return ret;
            }

            @Override
            public int getX() {
                return ChunkEmpty.this.getX() * chunkSize + chunkSize / 2;
            }

            @Override
            public int getY() {
                return ChunkEmpty.this.getY() * chunkSize + chunkSize / 2;
            }
        }
    }

    class ChunkFilled extends Chunk {
        int[][] counters = new int[chunkSize][chunkSize];

        public ChunkFilled(int x, int y) {
            super(x, y);
        }

        @Override
        public List<Cell> getEmptyBoundaryPoints(enumDirection d) {
            ArrayList<Cell> ret = new ArrayList<>(chunkSize);
            switch (d) {
                case EAST:
                    for (int i = 0; i < chunkSize; i++) {
                        if (counters[chunkSize - 1][i] == 0)
                            ret.add(new CellFilled(chunkSize - 1, i));
                    }
                    break;
                case WEST:
                    for (int i = 0; i < chunkSize; i++) {
                        if (counters[0][i] == 0)
                            ret.add(new CellFilled(0, i));
                    }
                    break;
                case NORTH:
                    for (int i = 0; i < chunkSize; i++) {
                        if (counters[i][chunkSize - 1] == 0)
                            ret.add(new CellFilled(i, chunkSize - 1));
                    }
                    break;
                case SOUTH:
                    for (int i = 0; i < chunkSize; i++) {
                        if (counters[i][0] == 0)
                            ret.add(new CellFilled(i, 0));
                    }
                    break;
                case ERROR:
                    break;
            }
            return ret;
        }

        @Override
        public Optional<Cell> getEmptyBoundaryPointAt(enumDirection d, int index) {
            switch (d) {
                case EAST:
                    if (counters[chunkSize - 1][index] == 0)
                        return Optional.of(new CellFilled(chunkSize - 1, index));
                    else
                        return Optional.empty();
                case WEST:
                    if (counters[0][index] == 0)
                        return Optional.of(new CellFilled(0, index));
                    else
                        return Optional.empty();
                case NORTH:
                    if (counters[index][chunkSize - 1] == 0)
                        return Optional.of(new CellFilled(index, chunkSize - 1));
                    else
                        return Optional.empty();
                case SOUTH:
                    if (counters[index][0] == 0)
                        return Optional.of(new CellFilled(index, 0));
                    else
                        return Optional.empty();
                default:
                    return Optional.empty();
            }
        }

        Cell getCell(int x, int y) {
            if (x >= this.x * chunkSize && x < (this.x + 1) * chunkSize - 1 &&
                    y >= this.y * chunkSize && y < (this.y + 1) * chunkSize - 1)
                return new CellFilled(x, y, false);
            else
                throw new IllegalStateException("Internal error");
        }

        @Override
        ChunkFilled generate() {
            return this;
        }

        @Override
        Chunk updateCell(int x, int y, boolean up) {
            if (up) {
                counters[x % chunkSize][y % chunkSize]++;
            } else if (counters[x % chunkSize][y % chunkSize] > 0) {
                counters[x % chunkSize][y % chunkSize]--;
            }
            return this;
        }

        class CellFilled implements Cell {
            int x;
            int y;
            int xRel;
            int yRel;

            public CellFilled(int x, int y) {
                this(x, y, true);
            }

            public CellFilled(int x, int y, boolean isRelative) {
                this.x = isRelative ? ChunkFilled.this.getX() * chunkSize + x : x;
                this.y = isRelative ? ChunkFilled.this.getY() * chunkSize + y : y;
                this.xRel = isRelative ? x : x - ChunkFilled.this.getX() * chunkSize;
                this.yRel = isRelative ? y : y - ChunkFilled.this.getY() * chunkSize;
            }

            @Override
            public List<Cell> getAccessibleCells() {
                List<Cell> ret = new ArrayList<>();
                if (xRel > 0) {
                    if (getCounterAt(xRel - 1, yRel) == 0)
                        ret.add(new CellFilled(xRel - 1, yRel));
                } else {
                    getChunkAt(enumDirection.WEST).ifPresent(chunk ->
                            chunk.getEmptyBoundaryPointAt(enumDirection.EAST, yRel).ifPresent(ret::add));
                }
                if (xRel < chunkSize - 1) {
                    if (getCounterAt(xRel + 1, yRel) == 0)
                        ret.add(new CellFilled(xRel + 1, yRel));
                } else {
                    getChunkAt(enumDirection.EAST).ifPresent(chunk ->
                            chunk.getEmptyBoundaryPointAt(enumDirection.WEST, yRel).ifPresent(ret::add));
                }
                if (yRel > 0) {
                    if (getCounterAt(xRel, yRel - 1) == 0)
                        ret.add(new CellFilled(xRel, yRel - 1));
                } else {
                    getChunkAt(enumDirection.NORTH).ifPresent(chunk ->
                            chunk.getEmptyBoundaryPointAt(enumDirection.SOUTH, yRel).ifPresent(ret::add));
                }
                if (yRel < chunkSize - 1) {
                    if (getCounterAt(xRel, yRel + 1) == 0)
                        ret.add(new CellFilled(xRel, yRel + 1));
                } else {
                    getChunkAt(enumDirection.SOUTH).ifPresent(chunk ->
                            chunk.getEmptyBoundaryPointAt(enumDirection.NORTH, yRel).ifPresent(ret::add));
                }
                return ret;
            }

            @Override
            public int getX() {
                return x;
            }

            @Override
            public int getY() {
                return y;
            }

            private int getCounterAt(int x, int y) {
                return counters[x][y];
            }


        }
    }
}
