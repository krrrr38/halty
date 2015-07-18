package com.krrrr38.halty

trait Generator[A, B <: GeneratorConfig] {
  /**
   * Generate contents from Chunk list.
   * @param blocks
   * @param config
   * @return
   */
  def generate(blocks: List[Block], config: B): A
}
