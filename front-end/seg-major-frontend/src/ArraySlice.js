export default class ArraySlice {
  #array;
  #from;
  length;

  constructor(array, from, length) {
    if (!(array instanceof Array)) throw new Error("array must be array");
    if (length < 0) throw new Error("length cannot be negative");
    if (length !== 0 && (from < 0 || from >= array.length)) throw new Error("from out of bounds");
    this.#array = array;
    this.#from = from;
    this.length = Math.min(length, array.length - from);
  }

  get(i) {
    if (i < 0 || i >= this.length) throw new Error("Index out of bounds");
    return this.#array[i + this.#from];
  }

  [Symbol.iterator]() {
    let index = this.#from - 1;
    const lastIndex = index + this.length;

    return {
      next: () => ({
        value: this.#array[++index],
        done: index > lastIndex
      })
    };
  }
}