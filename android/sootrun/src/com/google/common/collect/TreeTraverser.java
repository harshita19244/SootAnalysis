/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Views elements of a type {@code T} as nodes in a tree, and provides methods to traverse the trees
 * induced by this traverser.
 *
 * <p>For example, the tree
 *
 * <pre>{@code
 *        h
 *      / | \
 *     /  e  \
 *    d       g
 *   /|\      |
 *  / | \     f
 * a  b  c
 * }</pre>
 *
 * <p>can be iterated over in preorder (hdabcegf), postorder (abcdefgh), or breadth-first order
 * (hdegabcf).
 *
 * <p>Null nodes are strictly forbidden.
 *
 * <p><b>For Java 8 users:</b> Because this is an abstract class, not an interface, you can't use a
 * lambda expression to extend it:
 *
 * <pre>{@code
 * // won't work
 * TreeTraverser<NodeType> traverser = node -> node.getChildNodes();
 * }</pre>
 *
 * Instead, you can pass a lambda expression to the {@code using} factory method:
 *
 * <pre>{@code
 * TreeTraverser<NodeType> traverser = TreeTraverser.using(node -> node.getChildNodes());
 * }</pre>
 *
 * @author Louis Wasserman
 * @since 15.0
 * @deprecated Use {@link com.google.common.graph.Traverser} instead. All instance methods have
 *     their equivalent on the result of {@code Traverser.forTree(tree)} where {@code tree}
 *     implements {@code SuccessorsFunction}, which has a similar API as {@link #children} or can be
 *     the same lambda function as passed into {@link #using(Function)}.
 *     <p>This class is scheduled to be removed in October 2019.
 */
// TODO(b/68134636): Remove by 2019-10
@Deprecated
@Beta
@GwtCompatible
public abstract class TreeTraverser<T> {

  /**
   * Returns a tree traverser that uses the given function to navigate from a node to its children.
   * This is useful if the function instance already exists, or so that you can supply a lambda
   * expressions. If those circumstances don't apply, you probably don't need to use this; subclass
   * {@code TreeTraverser} and implement its {@link #children} method directly.
   *
   * @since 20.0
   * @deprecated Use {@link com.google.common.graph.Traverser#forTree} instead. If you are using a
   *     lambda, these methods have exactly the same signature.
   */
  @Deprecated
  public static <T> TreeTraverser<T> using(
      final Function<T, ? extends Iterable<T>> nodeToChildrenFunction) {
    checkNotNull(nodeToChildrenFunction);
    return new TreeTraverser<T>() {
      @Override
      public Iterable<T> children(T root) {
        return nodeToChildrenFunction.apply(root);
      }
    };
  }

  /** Returns the children of the specified node. Must not contain null. */
  public abstract Iterable<T> children(T root);

  /**
   * Returns an unmodifiable iterable over the nodes in a tree structure, using pre-order traversal.
   * That is, each node's subtrees are traversed after the node itself is returned.
   *
   * <p>No guarantees are made about the behavior of the traversal when nodes change while iteration
   * is in progress or when the iterators generated by {@link #children} are advanced.
   *
   * @deprecated Use {@link com.google.common.graph.Traverser#depthFirstPreOrder} instead, which has
   *     the same behavior.
   */
  @Deprecated
  public final FluentIterable<T> preOrderTraversal(final T root) {
    checkNotNull(root);
    return new FluentIterable<T>() {
      @Override
      public UnmodifiableIterator<T> iterator() {
        return preOrderIterator(root);
      }
    };
  }

  UnmodifiableIterator<T> preOrderIterator(T root) {
    return new PreOrderIterator(root);
  }

  private final class PreOrderIterator extends UnmodifiableIterator<T> {
    private final Deque<Iterator<T>> stack;

    PreOrderIterator(T root) {
      this.stack = new ArrayDeque<>();
      stack.addLast(Iterators.singletonIterator(checkNotNull(root)));
    }

    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public T next() {
      Iterator<T> itr = stack.getLast(); // throws NSEE if empty
      T result = checkNotNull(itr.next());
      if (!itr.hasNext()) {
        stack.removeLast();
      }
      Iterator<T> childItr = children(result).iterator();
      if (childItr.hasNext()) {
        stack.addLast(childItr);
      }
      return result;
    }
  }

  /**
   * Returns an unmodifiable iterable over the nodes in a tree structure, using post-order
   * traversal. That is, each node's subtrees are traversed before the node itself is returned.
   *
   * <p>No guarantees are made about the behavior of the traversal when nodes change while iteration
   * is in progress or when the iterators generated by {@link #children} are advanced.
   *
   * @deprecated Use {@link com.google.common.graph.Traverser#depthFirstPostOrder} instead, which
   *     has the same behavior.
   */
  @Deprecated
  public final FluentIterable<T> postOrderTraversal(final T root) {
    checkNotNull(root);
    return new FluentIterable<T>() {
      @Override
      public UnmodifiableIterator<T> iterator() {
        return postOrderIterator(root);
      }
    };
  }

  UnmodifiableIterator<T> postOrderIterator(T root) {
    return new PostOrderIterator(root);
  }

  private static final class PostOrderNode<T> {
    final T root;
    final Iterator<T> childIterator;

    PostOrderNode(T root, Iterator<T> childIterator) {
      this.root = checkNotNull(root);
      this.childIterator = checkNotNull(childIterator);
    }
  }

  private final class PostOrderIterator extends AbstractIterator<T> {
    private final ArrayDeque<PostOrderNode<T>> stack;

    PostOrderIterator(T root) {
      this.stack = new ArrayDeque<>();
      stack.addLast(expand(root));
    }

    @Override
    protected T computeNext() {
      while (!stack.isEmpty()) {
        PostOrderNode<T> top = stack.getLast();
        if (top.childIterator.hasNext()) {
          T child = top.childIterator.next();
          stack.addLast(expand(child));
        } else {
          stack.removeLast();
          return top.root;
        }
      }
      return endOfData();
    }

    private PostOrderNode<T> expand(T t) {
      return new PostOrderNode<T>(t, children(t).iterator());
    }
  }

  /**
   * Returns an unmodifiable iterable over the nodes in a tree structure, using breadth-first
   * traversal. That is, all the nodes of depth 0 are returned, then depth 1, then 2, and so on.
   *
   * <p>No guarantees are made about the behavior of the traversal when nodes change while iteration
   * is in progress or when the iterators generated by {@link #children} are advanced.
   *
   * @deprecated Use {@link com.google.common.graph.Traverser#breadthFirst} instead, which has the
   *     same behavior.
   */
  @Deprecated
  public final FluentIterable<T> breadthFirstTraversal(final T root) {
    checkNotNull(root);
    return new FluentIterable<T>() {
      @Override
      public UnmodifiableIterator<T> iterator() {
        return new BreadthFirstIterator(root);
      }
    };
  }

  private final class BreadthFirstIterator extends UnmodifiableIterator<T>
      implements PeekingIterator<T> {
    private final Queue<T> queue;

    BreadthFirstIterator(T root) {
      this.queue = new ArrayDeque<T>();
      queue.add(root);
    }

    @Override
    public boolean hasNext() {
      return !queue.isEmpty();
    }

    @Override
    public T peek() {
      return queue.element();
    }

    @Override
    public T next() {
      T result = queue.remove();
      Iterables.addAll(queue, children(result));
      return result;
    }
  }
}
