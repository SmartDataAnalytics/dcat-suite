package org.aksw.dcat_suite.app.fs2;

import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;


public abstract class PathBase<T extends Path>
    implements Path
{
    protected boolean isAbsolute;
    protected List<String> segments;

    public abstract Path newPath(boolean isAbsolute, List<String> segments);
    public abstract T requireSubType(Path other);


    public PathBase(boolean isAbsolute, List<String> segments) {
        super();
        this.isAbsolute = isAbsolute;
        this.segments = segments;
    }

    protected abstract PathOps getPathOpts();


    @Override
    public URI toUri() {
        return getPathOpts().toUri(segments);
    }

    @Override
    public Path toAbsolutePath() {
        return newPath(true, getPathOpts().getBasePathSegments()).resolve(this);

//		return getPathOpts().getAbsoluteBasePath().resolve(this);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    }

    @Override
    public Path getRoot() {
        return newPath(true, Collections.<String>emptyList());
    }

    @Override
    public Path getFileName() {
        Path result = segments.isEmpty()
            ? null
            : newPath(false, Collections.singletonList(segments.get(segments.size() - 1)));
        return result;
    }

    @Override
    public Path getParent() {
        Path result = segments.isEmpty()
                ? null
                : newPath(isAbsolute(), segments.subList(0, segments.size() - 1));
            return result;
    }

    @Override
    public int getNameCount() {
        return segments.size();
    }

    @Override
    public Path getName(int index) {
        return newPath(false, Collections.singletonList(segments.get(index)));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return newPath(false, segments.subList(endIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        boolean result;
        int n = other.getNameCount();
        if (n <= getNameCount()) {
            for (int i = 0; i < n; ++i) {
                String part = other.getName(i).toString();
                if (!Objects.equals(segments.get(i), part)) {
                    result = false;
                }
            }
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public boolean endsWith(Path other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Path normalize() {
        List<String> tmp = new ArrayList<>();

        Iterator<String> it = segments.iterator();
        while (it.hasNext()) {
            String item = it.next();

            if (isParentToken(item)) {
                if (!tmp.isEmpty()) {
                    // Remove the last item from the newSteps
                    ListIterator<String> delIt = tmp.listIterator(tmp.size());
                    String seenItem = delIt.previous();

                    if (isParentToken(seenItem)) {
                        tmp.add(item);
                    } else {
                        delIt.remove();
                    }
                } else {
                    tmp.add(item);
                }
            } else {
                tmp.add(item);
            }
        }

        return newPath(isAbsolute(), tmp);
    }

    public boolean isParentToken(String str) {
        return str.equals(getPathOpts().getParentToken());
    }

    public static List<String> toList(Path path) {
        int n = path.getNameCount();
        List<String> result = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            Path tmp = path.getName(i);
            String str = tmp.toString();
            result.add(str);
        }

        return result;
    }

    @Override
    public Path resolve(Path other) {
        Path result;
        if (other.isAbsolute()) {
            result = other;
        } else {
            List<String> newSteps = new ArrayList<>(segments.size() + other.getNameCount());
            newSteps.addAll(segments);
            newSteps.addAll(toList(other));
            result = newPath(isAbsolute, newSteps);
        }
        return result;
    }

    @Override
    public Path relativize(Path other) {
        return newPath(isAbsolute, relativize(this.segments, toList(other), getPathOpts().getParentToken()));
    }

//	protected String getParentToken() {
//		return "..";
//	}

//	@Override
//	public URI toUri() {
//
//		// getFileSystem().
//		// new URI(schema, authority, path, qury, fragment);
//	}

//	@Override
//	public Path toAbsolutePath() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Path toRealPath(LinkOption... options) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path other) {
        int result;
        if (other instanceof PathBase) {
            PathBase<?> o = (PathBase<?>)other;

            // Sort absolute paths first
            result = (o.isAbsolute ? 0 : 1) - (isAbsolute ? 0 : 1);
            if (result == 0) {
                result = compareLists(segments, o.segments);
            }
        } else {
            result = -1;
        }
        return result;
    }

    public static <T extends Comparable<T>> int compareLists(List<T> a, List<T> b) { //, Comparator<T> comparator) {
        int result = 0;
        int as = a.size();
        int bs = b.size();
        int n = Math.min(a.size(), b.size());

        for (int i = 0; i < n; ++i) {
            T ai = a.get(i);
            T bi = b.get(i);

            //result = comparator.compare(ai, bi);
            result = ai.compareTo(bi);

            if (result != 0) {
                break;
            }
        }

        // If elements were equal then compare by length, shorter first
        result = result != 0
            ? result
            : bs - as;

        return result;
    }

    public String relativeString() {
        String sep = getFileSystem().getSeparator();
        String result = segments.stream().collect(Collectors.joining(sep));
        return result;
    }

    @Override
    public String toString() {
        String relPathStr = relativeString();
        String result = (isAbsolute() ? "/" : "") + relPathStr;
        return result;
        // getFileSystem().getRootDirectories()
    }


    public static <T> List<T> relativize(List<T> a, List<T> b, T parentToken) {
        List<T> result = new ArrayList<>();

        // Find number of common elements
        int as = a.size();
        int bs = b.size();

        int i = 0;
        while (i < as && i < bs && Objects.equals(a.get(i), b.get(i))) {
            i++;
        }

        // Add as many 'go to parent folder' items as there are remaining items
        // in a starting from i
        for (int j = i; j < as; j++) {
            result.add(parentToken);
        }

        // Add the elements of b starting with index i
        result.addAll(b.subList(i, bs - 1));
        return result;
    }
}
